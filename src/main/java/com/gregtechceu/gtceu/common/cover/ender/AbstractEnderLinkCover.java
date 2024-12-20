package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.*;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractEnderLinkCover<T extends VirtualEntry> extends CoverBehavior
                                            implements IUICover, IControllable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AbstractEnderLinkCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);
    public static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("[0-9a-fA-F]*");
    protected final ConditionalSubscriptionHandler subscriptionHandler;
    @Persisted
    @DescSynced
    protected String colorStr = VirtualEntry.DEFAULT_COLOR;
    @Persisted
    @DescSynced
    protected String description = VirtualEntry.DEFAULT_COLOR;
    @Persisted
    @DescSynced
    private IMachineOwner owner;
    @Getter
    @Persisted
    @DescSynced
    protected boolean isPrivate = false;
    @Persisted
    @Getter
    protected boolean isWorkingEnabled = true;
    @Persisted
    @DescSynced
    @Getter
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;
    @Persisted
    @DescSynced
    @Getter
    @RequireRerender
    protected IO io = IO.OUT;
    @DescSynced
    boolean isAnyChanged = false;
    protected VirtualEntryWidget virtualEntryWidget;

    public AbstractEnderLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        subscriptionHandler.unsubscribe();
    }

    protected abstract VirtualEntry getEntry();

    protected abstract void setEntry(VirtualEntry entry);

    protected abstract Stream<VirtualEntry> getEntries();

    public abstract void clearEntries();

    @Override
    public abstract boolean canAttach();

    @Override
    public void onAttached(@NotNull ItemStack itemStack, @NotNull ServerPlayer player) {
        super.onAttached(itemStack, player);
        if (coverHolder instanceof MachineCoverContainer mcc) {
            var owner = mcc.getMachine().getHolder().getOwner();
            if (owner != null) this.owner = owner;
        }
    }

    @Override
    public void onUIClosed() {
        if (virtualEntryWidget != null)
            virtualEntryWidget = null;
        clearEntries();
    }

    protected final String getChannelName() {
        return identifier() + this.colorStr;
    }

    protected final String getChannelName(VirtualEntry entry) {
        return identifier() + entry.getColorStr();
    }

    protected abstract String identifier();

    protected void setChannelName(String name) {
        if (name == null || name.isEmpty()) return;
        var reg = VirtualEnderRegistry.getInstance();
        if (reg == null) return;
        reg.deleteEntryIf(getOwner(), getEntryType(), this.colorStr, VirtualEntry::canRemove);
        this.colorStr = VirtualEntry.formatColorString(name);
        setVirtualEntry(reg);
    }

    protected void setPrivate(boolean isPrivate) {
        if (isPrivate == this.isPrivate) return;
        var reg = VirtualEnderRegistry.getInstance();
        if (reg == null) return;
        reg.deleteEntryIf(getOwner(), getEntryType(), this.colorStr, VirtualEntry::canRemove);
        this.isPrivate = isPrivate;
        setVirtualEntry(reg);
    }

    private void setVirtualEntry(VirtualEnderRegistry reg) {
        setEntry(reg.getOrCreateEntry(getOwner(), getEntryType(), this.colorStr));
        getEntry().setColor(this.colorStr);
        this.isAnyChanged = true;
        subscriptionHandler.updateSubscription();
    }

    protected abstract EntryTypes<T> getEntryType();

    public UUID getOwner() {
        return isPrivate ? owner.getPlayerUUID() : null;
    }

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 != 0) return;

        if (isWorkingEnabled()) {
            transfer();
        }

        if (isAnyChanged) {
            if (virtualEntryWidget != null)
                virtualEntryWidget.update();
            isAnyChanged = false;
        }

        subscriptionHandler.updateSubscription();
    }

    protected abstract void transfer();

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            subscriptionHandler.updateSubscription();
        }
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
            subscriptionHandler.updateSubscription();
        }
    }

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        subscriptionHandler.updateSubscription();
    }

    @Override
    public Widget createUIWidget() {
        virtualEntryWidget = new VirtualEntryWidget(this);
        return virtualEntryWidget;
    }

    @Nullable
    protected FilterHandler<?, ?> getFilterHandler(){
        return null;
    }

    protected abstract Widget addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height);

    protected abstract String getUITitle();

    protected int getColorStr() {
        return VirtualEntry.parseColor(this.colorStr);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected static class VirtualEntryWidget extends WidgetGroup {
        private final AbstractEnderLinkCover<?> cover;

        private static final int SMALL_WIDGET_WIDTH = 20;
        private static final int WIDGET_HEIGHT = 20;
        private static final int GROUP_WIDTH = 176;
        private static final int TOTAL_WIDTH = 156;
        private static final int BUTTON_SIZE = 16;
        private final MutableBoolean showChannels;
        private final WidgetGroup mainGroup;
        private final DraggableScrollableWidgetGroup channelsGroup;
        private final WidgetGroup mainChannelGroup;

        VirtualEntryWidget(AbstractEnderLinkCover<?> cover) {
            super(0, 0, GROUP_WIDTH, 137);
            this.cover = cover;
            this.showChannels = new MutableBoolean(false);
            mainGroup = new WidgetGroup(0, 0, GROUP_WIDTH, 137);
            channelsGroup = new DraggableScrollableWidgetGroup(0, 20, 170, 110)
                    .setYScrollBarWidth(2).setYBarStyle(null, ColorPattern.T_WHITE.rectTexture().setRadius(1));
            mainChannelGroup = new WidgetGroup(10, 20, 156, 20);
            initWidgets();
        }

        public void update() {
            var reg = VirtualEnderRegistry.getInstance();
            if (reg == null) return;
            widgets.clear();
            mainGroup.widgets.clear();
            channelsGroup.widgets.clear();
            mainChannelGroup.widgets.clear();
            initWidgets();
            this.detectAndSendChanges();
        }

        private void initWidgets() {
            int currentX = 0;

            this.addWidget(new LabelWidget(10, 5, cover.getUITitle()));
            this.addWidget(createToggleButton());
            this.addWidget(mainGroup);
            this.addWidget(channelsGroup.setVisible(false));

            var toggleButtonWidget = createToggleButtonForPrivacy(currentX);
            mainChannelGroup.addWidget(toggleButtonWidget);
            currentX += SMALL_WIDGET_WIDTH + 2;
            mainChannelGroup.addWidget(createColorBlockWidget(currentX));
            currentX += SMALL_WIDGET_WIDTH + 2;
            mainChannelGroup.addWidget(createConfirmTextInputWidget(currentX));

            mainGroup.addWidget(mainChannelGroup);
            mainGroup.addWidget(createWorkingEnabledButton());
            addEnumSelectorWidgets();
            mainGroup.addWidget(cover.addVirtualEntryWidget(cover.getEntry(), 146, 20, 20, 20));

            if (cover.getFilterHandler() != null) {
                mainGroup.addWidget(cover.getFilterHandler().createFilterSlotUI(117, 108));
                mainGroup.addWidget(cover.getFilterHandler().createFilterConfigUI(10, 72, 156, 60));
            }

            addChannelWidgets();
        }

        @Contract(" -> new")
        private @NotNull ToggleButtonWidget createToggleButton() {
            return new ToggleButtonWidget(156, 5, 10, 10, GuiTextures.TOGGLE_BUTTON_BACK,
                    showChannels::getValue, cd -> {
                showChannels.setValue(!showChannels.getValue());
                mainGroup.setVisible(showChannels.isFalse());
                channelsGroup.setVisible(showChannels.isTrue());
            });
        }

        private @NotNull ToggleButtonWidget createToggleButtonForPrivacy(int currentX) {
            ToggleButtonWidget toggleButtonWidget = new ToggleButtonWidget(currentX, 0, SMALL_WIDGET_WIDTH,
                    WIDGET_HEIGHT, GuiTextures.BUTTON_PUBLIC_PRIVATE, cover::isPrivate, null);
            toggleButtonWidget.setHoverTooltips(cover.isPrivate ?
                    "cover.ender_link.permission.public" : "cover.ender_link.permission.private");
            toggleButtonWidget.setOnPressCallback((clickData, isPrivate) -> {
                cover.setPrivate(isPrivate);
                cover.isAnyChanged = true;
            });
            return toggleButtonWidget;
        }

        private ColorBlockWidget createColorBlockWidget(int currentX) {
            return new ColorBlockWidget(currentX, 0, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT)
                    .setColorSupplier(cover::getColorStr);
        }

        private ConfirmTextInputWidget createConfirmTextInputWidget(int currentX) {
            int GROUP_X = 10;
            int textInputWidth = (GROUP_WIDTH - GROUP_X * 2) - currentX - SMALL_WIDGET_WIDTH - 2;
            return new ConfirmTextInputWidget(currentX, 0, textInputWidth, WIDGET_HEIGHT,
                    cover.colorStr,
                    cover::setChannelName,
                    text -> {
                        if (text == null || !COLOR_INPUT_PATTERN.matcher(text).matches() || text.length() > 8) {
                            return VirtualTank.DEFAULT_COLOR;
                        }
                        return text;
                    }).setHoverText(cover.description);
        }

        @Contract(" -> new")
        private @NotNull ToggleButtonWidget createWorkingEnabledButton() {
            return new ToggleButtonWidget(116, 82, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT,
                    GuiTextures.BUTTON_POWER, cover::isWorkingEnabled, cover::setWorkingEnabled);
        }

        private void addEnumSelectorWidgets() {
            mainGroup.addWidget(new EnumSelectorWidget<>(146, 82, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT,
                    List.of(IO.IN, IO.OUT), cover.io, cover::setIo));
            mainGroup.addWidget(new EnumSelectorWidget<>(146, 107, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT,
                    ManualIOMode.VALUES, cover.manualIOMode, cover::setManualIOMode)
                    .setHoverTooltips("cover.universal.manual_import_export.mode.description"));
        }

        private void addChannelWidgets() {
            int y = 0;
            SelectableWidgetGroup selectedWidget = null;
            for (var entry : cover.getEntries().sorted(Comparator.comparing(VirtualEntry::getColorStr)).toList()) {
                SelectableWidgetGroup channelWidget = createChannelWidget(entry, 10, y);
                if (cover.getChannelName(entry).equals(cover.getChannelName())) {
                    selectedWidget = channelWidget;
                }
                channelsGroup.addWidget(channelWidget);
                y += 22;
            }
            channelsGroup.setSelected(selectedWidget);
            if (selectedWidget != null) selectedWidget.onSelected();
        }

        private @NotNull SelectableWidgetGroup createChannelWidget(@NotNull VirtualEntry entry, int x, int y) {
            int currentX = 0;
            int MARGIN = 2;
            int availableWidth = TOTAL_WIDTH - (BUTTON_SIZE + MARGIN) * 2;

            TextBoxWidget textBoxWidget = new TextBoxWidget(BUTTON_SIZE + MARGIN, 4, availableWidth, List.of(entry.getColorStr())).setCenter(true);
            SelectableWidgetGroup channelGroup = new SelectableWidgetGroup(x, y, TOTAL_WIDTH, BUTTON_SIZE).setOnSelected(group -> {
                if (cover.getChannelName().equals(cover.getChannelName(entry))) return;
                writeClientAction(0, buffer -> {
                    // send new channel name to server
                    String newChannelColorStr = entry.getColorStr();
                    buffer.writeUtf(newChannelColorStr);
                });
                playButtonClickSound();
            }).setSelectedTexture(1, -1);

            // Color block
            ColorBlockWidget colorBlockWidget =
                    new ColorBlockWidget(currentX, 0, BUTTON_SIZE, BUTTON_SIZE).setCurrentColor(entry.getColor());
            channelGroup.addWidget(colorBlockWidget);
            currentX += BUTTON_SIZE + MARGIN;

            // Text box
            channelGroup.addWidget(textBoxWidget);
            currentX += availableWidth + MARGIN;

            // Slot
            Widget slotWidget = cover.addVirtualEntryWidget(entry, currentX, 0, BUTTON_SIZE, BUTTON_SIZE);
            channelGroup.addWidget(slotWidget);

            return channelGroup;
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            super.handleClientAction(id, buffer);
            if (id == 0) {
                String newChannelColorStr = buffer.readUtf();
                cover.setChannelName(newChannelColorStr);
            }
        }
    }
}
