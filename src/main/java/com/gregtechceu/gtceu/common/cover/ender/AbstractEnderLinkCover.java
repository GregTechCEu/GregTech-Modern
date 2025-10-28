package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.ColorBlockWidget;
import com.gregtechceu.gtceu.api.gui.widget.ConfirmTextInputWidget;
import com.gregtechceu.gtceu.api.gui.widget.EnumSelectorWidget;
import com.gregtechceu.gtceu.api.gui.widget.ToggleButtonWidget;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.DynamicSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.EnumSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.GenericSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.StringSyncValue;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Column;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Row;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufAdapter;

import com.lowdragmc.lowdraglib.gui.editor.ColorPattern;
import com.lowdragmc.lowdraglib.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractEnderLinkCover<T extends VirtualEntry> extends CoverBehavior
                                            implements IMuiCover, IControllable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AbstractEnderLinkCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);
    public static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("^[0-9a-fA-F]{0,8}$");

    protected final ConditionalSubscriptionHandler subscriptionHandler;

    @Persisted
    @DescSynced
    @Getter
    protected String colorStr = VirtualEntry.DEFAULT_COLOR;
    @Getter
    @Persisted
    @DescSynced
    protected Permissions permission = Permissions.PUBLIC;
    @Persisted
    @Getter
    protected boolean isWorkingEnabled = true;
    @Getter
    @Persisted
    @DescSynced
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected IO io = IO.OUT;
    protected VirtualEntryWidget virtualEntryWidget;
    @DescSynced
    boolean isAnyChanged = false;
    @Getter
    @Setter
    private boolean isChannelListActive;

    public AbstractEnderLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isSubscriptionActive);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public abstract boolean canAttach();

    @Override
    public void onAttached(@NotNull ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);
    }

    @Override
    public void onRemoved() {
        super.onRemoved();
        subscriptionHandler.unsubscribe();
        if (!coverHolder.isRemote()) {
            VirtualEnderRegistry.getInstance()
                    .deleteEntryIf(getOwner(), getEntryType(), getChannelName(), VirtualEntry::canRemove);
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        subscriptionHandler.unsubscribe();
        if (!coverHolder.isRemote()) {
            VirtualEnderRegistry.getInstance()
                    .deleteEntryIf(getOwner(), getEntryType(), getChannelName(), VirtualEntry::canRemove);
        }
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {
        if (this.isWorkingEnabled != isWorkingAllowed) {
            this.isWorkingEnabled = isWorkingAllowed;
            subscriptionHandler.updateSubscription();
        }
    }

    @Override
    public ParentWidget createCoverUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        GenericSyncValue<List<VirtualEntry>> entriesSyncer = new GenericSyncValue<>(this::getVirtualEntries, this::setVirtualEntries,
                new VirtualEntryListAdapter());
        DynamicSyncHandler entryListSyncer = new DynamicSyncHandler().widgetProvider((manager, packet) -> {
            ListWidget<IWidget, ?> list = new ListWidget<>();
            if (packet == null) return new EmptyWidget();
            populateChannelList(list, packet);
            return list.childSeparator(GTGuiTextures.SEPERATOR_SIMPLE.asIcon().size(116, 5).margin(12, 0))
                    .size(162, 58);
        });
        entriesSyncer.setChangeListener(() -> entryListSyncer.notifyUpdate(packet -> {
            List<VirtualEntry> entries = entriesSyncer.getValue();
            packet.writeInt(entries.size());
            for (VirtualEntry entry : entries) {
                packet.writeNbt(entry.serializeNBT());
            }
        }));
        syncManager.syncValue("CLA", new BooleanSyncValue(this::isChannelListActive, this::setChannelListActive));
        syncManager.syncValue("entries", entriesSyncer);
        return new Column()
                .child(IMuiCover.createTitleRow(this.getAttachItem())
                        .child(new ToggleButton().syncHandler("CLA")
                                .overlay(GTGuiTextures.MORE)
                                .tooltip(t -> t
                                        .addLine(Component.translatable("cover.ender_fluid_link.tooltip.list_button")))
                                .marginLeft(4)
                                .size(16, 16)))
                .child(createChannelNameRow(data, syncManager, settings).setEnabledIf(f -> !isChannelListActive))
                .child(createDescriptionField(data, syncManager, settings).setEnabledIf(f -> !isChannelListActive))
                .child(createSettingsRow(data, syncManager, settings).setEnabledIf(f -> !isChannelListActive))
                .child(new DynamicSyncedWidget<>().size(162, 58).syncHandler(entryListSyncer))
                .rightRel(0.5F)
                .top(3)
                .childPadding(3)
                .collapseDisabledChild()
                .coverChildren();
    }

    public void populateChannelList(ListWidget<IWidget, ?> list, FriendlyByteBuf packet) {
        int size = packet.readInt();
        for (int i = 0; i < size; i++) {
            VirtualEntry entry = getEntryType().createInstance(packet.readNbt());
            list.child(createVirtualEntryRow(entry));
        }
    }

    public Flow createVirtualEntryRow(VirtualEntry entry) {
        return new Row()
                .child(createColorBlock(entry::getColor, 18).asWidget()
                        .tooltip(t -> t.addLine(entry.getColorStr()))
                        .size(18, 18))
                .child(IKey.str(entry.getDescription()).asWidget()
                        .size(92, 12))
                .child(createVirtualEntryWidget(entry, 18, 18, false))
                .child(new com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget<>().overlay(GTGuiTextures.BUTTON_CROSS)
                        .onMousePressed((x, y, button) -> {
                            MouseData mouseData = MouseData.create(button);
                            if (mouseData.mouseButton() == 1) {
                                VirtualEnderRegistry.getInstance()
                                        .getEntry(getOwner(), getEntryType(), entry.getColorStr()).setDescription("");
                                // entry.setDescription("");
                                return true;
                            }
                            return false;
                        }))
                .alignX(0F)
                .childPadding(3)
                .coverChildren();
    }

    public Flow createChannelNameRow(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new Row().child(createColorBlock(this::getColor, 18).asWidget())
                .child(new TextFieldWidget().value(new StringSyncValue(this::getColorStr, this::setChannelName))
                        .setPattern(COLOR_INPUT_PATTERN)
                        .hintText(Component.translatable("cover.ender_link.channel_name"))
                        .size(120, 16))
                .child(createVirtualEntryWidget(this.getEntry(), 18, 18, true))
                .childPadding(3)
                .coverChildren();
    }

    public TextFieldWidget createDescriptionField(SidedPosGuiData data, PanelSyncManager syncManager,
                                                  UISettings settings) {
        return new TextFieldWidget().value(new StringSyncValue(getEntry()::getDescription, getEntry()::setDescription))
                .hintText(Component.translatable("cover.ender_link.channel_description"))
                .widthRel(1F)
                .height(16);
    }

    public Flow createSettingsRow(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        return new Row()
                // Power button
                .child(new ToggleButton().value(new BooleanSyncValue(this::isWorkingEnabled, this::setWorkingEnabled))
                        // TODO: once the branch that has power overlays defined in GTGuiTextures is merged, we need to
                        // TODO: replace this temporary usage with those. or keep them. they are pretty cool
                        .overlay(false, GTGuiTextures.PLAY)
                        .overlay(true, GTGuiTextures.PLAY))
                // Import / Export button
                .child(new com.gregtechceu.gtceu.api.mui.widgets.CycleButtonWidget()
                        // There are "3" states here because otherwise there is a noticeable lag on the client when it
                        // goes from OUT to IN and initially doesn't know to go back to IN (because the enum has 4
                        // states) so it displays no overlay or tooltip. Very annoying.
                        .stateCount(3)
                        .stateOverlay(IO.IN, GTGuiTextures.IO_IMPORT)
                        .stateOverlay(IO.OUT, GTGuiTextures.IO_EXPORT)
                        .stateOverlay(IO.BOTH, GTGuiTextures.IO_IMPORT)
                        .tooltip(0, t -> t.addLine(Component.translatable(IO.IN.tooltip)))
                        .tooltip(1, t -> t.addLine(Component.translatable(IO.OUT.tooltip)))
                        .tooltip(2, t -> t.addLine(Component.translatable(IO.IN.tooltip)))
                        .value(new EnumSyncValue<>(IO.class, this::getIo, this::reverseIO)))
                // Public / Private button
                .child(new com.gregtechceu.gtceu.api.mui.widgets.CycleButtonWidget()
                        .stateCount(2)
                        .stateOverlay(Permissions.PUBLIC, Permissions.PUBLIC.icon)
                        .stateOverlay(Permissions.PRIVATE, Permissions.PRIVATE.icon)
                        .tooltip(0, t -> t.addLine(IKey.lang(Permissions.PUBLIC.tooltip + ".0"))
                                .addLine(IKey.lang(Permissions.PUBLIC.tooltip + ".1")))
                        .tooltip(1, t -> t.addLine(IKey.lang(Permissions.PRIVATE.tooltip)))
                        .value(new EnumSyncValue<>(Permissions.class, this::getPermission,
                                this::setPermission)))
                // Manual IO button
                .child(new com.gregtechceu.gtceu.api.mui.widgets.CycleButtonWidget()
                        .stateCount(3)
                        .stateOverlay(ManualIOMode.DISABLED, GTGuiTextures.MANUAL_IO_DISABLED)
                        .stateOverlay(ManualIOMode.FILTERED, GTGuiTextures.MANUAL_IO_FILTERED)
                        .stateOverlay(ManualIOMode.UNFILTERED, GTGuiTextures.MANUAL_IO_UNFILTERED)
                        .tooltip(0, t -> t.addLine(Component.translatable(ManualIOMode.DISABLED.getTooltip())))
                        .tooltip(1, t -> t.addLine(Component.translatable(ManualIOMode.FILTERED.getTooltip())))
                        .tooltip(2, t -> t.addLine(Component.translatable(ManualIOMode.UNFILTERED.getTooltip())))
                        .value(new EnumSyncValue<>(ManualIOMode.class, this::getManualIOMode, this::setManualIOMode)))
                // TODO: Add Filter slot here once kathryne's ui gets merged
                .child(new ToggleButton().overlay(GTGuiTextures.EXCLAMATION)
                        .size(18, 18))
                .childPadding(18)
                .widthRel(1F)
                .coverChildrenHeight();
    }

    public IDrawable createColorBlock(IntSupplier colorSupplier, int size) {
        return IDrawable.of(
                // Border
                (context, x, y, w, h, widgetTheme) -> new Rectangle().setColor(Color.BLACK.main)
                        .draw(context, x, y, size, size, widgetTheme),
                // Colored block
                (context, x, y, w, h, widgetTheme) -> new Rectangle().setColor(colorSupplier.getAsInt())
                        .draw(context, x + 1, y + 1, size - 2, size - 2, widgetTheme));
    }

    public List<VirtualEntry> getVirtualEntries() {
        List<VirtualEntry> entries = new ArrayList<>();
        for (String entryName : VirtualEnderRegistry.getInstance().getEntryNames(getOwner(), getEntryType())) {
            entries.add(VirtualEnderRegistry.getInstance().getEntry(getOwner(), getEntryType(), entryName));
        }
        return entries;
    }

    public void setVirtualEntries(List<VirtualEntry> entries) {
        for (VirtualEntry entry : entries) {
            VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(), getEntryType(), entry.getColorStr())
                    .setDescription(entry.getDescription());
        }
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    private void reverseIO(IO io) {
        setIo(IO.values()[io.ordinal() % 2]);
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
            subscriptionHandler.updateSubscription();
        }
    }

    public UUID getOwner() {
        if (permission == Permissions.PRIVATE && coverHolder instanceof MachineCoverContainer mcc) {
            var owner = mcc.getMachine().getOwner();
            return owner != null ? owner.getPlayerUUID() : null;
        }
        return null;
    }

    protected boolean isSubscriptionActive() {
        return isWorkingEnabled();
    }

    protected abstract String identifier();

    protected abstract VirtualEntry getEntry();

    protected abstract void setEntry(VirtualEntry entry);

    protected final String getChannelName() {
        return identifier() + this.colorStr;
    }

    protected void setChannelName(String name) {
        if (coverHolder.isRemote()) return;
        VirtualEnderRegistry.getInstance().deleteEntryIf(getOwner(), getEntryType(), getChannelName(),
                VirtualEntry::canRemove);
        this.colorStr = name;
        if (colorStr.length() < 8) {
            colorStr += "F".repeat(8 - colorStr.length());
        }
        setVirtualEntry();
    }

    protected final String getChannelName(VirtualEntry entry) {
        return identifier() + entry.getColorStr();
    }

    protected void setPermission(Permissions permission) {
        if (coverHolder.isRemote()) return;
        VirtualEnderRegistry.getInstance().deleteEntryIf(getOwner(), getEntryType(), getChannelName(),
                VirtualEntry::canRemove);
        this.permission = permission;
        setVirtualEntry();
    }

    protected void setVirtualEntry() {
        setEntry(VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(), getEntryType(), getChannelName()));
        getEntry().setColor(this.colorStr);
        this.isAnyChanged = true;
        subscriptionHandler.updateSubscription();
    }

    protected abstract EntryTypes<T> getEntryType();

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 != 0) return;

        if (isWorkingEnabled() && !coverHolder.isRemote()) {
            var entry = VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(), getEntryType(),
                    getChannelName());
            if (!entry.getColorStr().equals(this.colorStr)) {
                entry.setColor(this.colorStr);
            }
            if (!getEntry().equals(entry)) {
                setEntry(entry);
            }
            transfer();
        }

        if (isAnyChanged) {
            if (virtualEntryWidget != null) virtualEntryWidget.update();
            isAnyChanged = false;
        }
        subscriptionHandler.updateSubscription();
    }

    protected abstract void transfer();

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        subscriptionHandler.updateSubscription();
    }

    @Nullable
    protected FilterHandler<?, ?> getFilterHandler() {
        return null;
    }

    protected abstract IWidget createVirtualEntryWidget(VirtualEntry entry, int width, int height,
                                                        boolean interactable);

    protected abstract String getUITitle();

    protected int getColor() {
        return VirtualEntry.parseColor(this.colorStr);
    }

    protected enum Permissions {

        PUBLIC("cover.ender_fluid_link.private.tooltip.disabled", GTGuiTextures.OVERLAY_LOCK_OPEN),
        PRIVATE("cover.ender_fluid_link.private.tooltip.enabled", GTGuiTextures.OVERLAY_LOCK_CLOSED);

        @Getter
        private final String tooltip;
        @Getter
        private final UITexture icon;

        Permissions(String tooltip, UITexture icon) {
            this.tooltip = tooltip;
            this.icon = icon;
        }
    }

    protected static class VirtualEntryWidget extends WidgetGroup {

        private static final int WIDGET_BOARD = 20;
        private static final int GROUP_WIDTH = 176;
        private static final int TOTAL_WIDTH = 156;
        private static final int BUTTON_SIZE = 16;
        private final AbstractEnderLinkCover<?> cover;
        private final MutableBoolean showChannels;
        private final WidgetGroup mainGroup;
        private final WidgetGroup mainChannelGroup;
        private final DraggableScrollableWidgetGroup channelsGroup; // client only

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
            if (isRemote()) return;
            widgets.clear();
            mainGroup.widgets.clear();
            channelsGroup.widgets.clear();
            mainChannelGroup.widgets.clear();
            initWidgets();
            this.detectAndSendChanges();
        }

        private void initWidgets() {
            int currentX = 0;
            final var titleGroup = new WidgetGroup(10, 5, GROUP_WIDTH, 20);

            this.addWidget(titleGroup);
            this.addWidget(mainGroup);
            this.addWidget(channelsGroup.setVisible(false));

            titleGroup.addWidget(createToggleButton());
            titleGroup.addWidget(new LabelWidget(15, 3, cover.getUITitle()));

            var toggleButtonWidget = createToggleButtonForPrivacy(currentX);
            mainChannelGroup.addWidget(toggleButtonWidget);
            currentX += WIDGET_BOARD + 2;
            mainChannelGroup.addWidget(createColorBlockWidget(currentX));
            currentX += WIDGET_BOARD + 2;
            mainChannelGroup.addWidget(createConfirmTextInputWidget(currentX));

            mainChannelGroup.addWidget(new ConfirmTextInputWidget(0, WIDGET_BOARD + 2, GROUP_WIDTH - WIDGET_BOARD,
                    WIDGET_BOARD, cover.getEntry().getDescription(), cover.getEntry()::setDescription,
                    t -> t == null ? "" : t, null).setTooltip("cover.ender_fluid_link.tooltip.channel_description"));

            mainGroup.addWidget(mainChannelGroup);
            mainGroup.addWidget(createWorkingEnabledButton());
            addEnumSelectorWidgets();
            // mainGroup.addWidget(cover.createVirtualEntryWidget(cover.getEntry(), 146, WIDGET_BOARD, WIDGET_BOARD,
            // WIDGET_BOARD, true));

            if (cover.getFilterHandler() != null) {
                mainGroup.addWidget(cover.getFilterHandler().createFilterSlotUI(117, 108));
                mainGroup.addWidget(cover.getFilterHandler().createFilterConfigUI(10, 72, 156, 60));
            }
        }

        @Contract(" -> new")
        private @NotNull ToggleButtonWidget createToggleButton() {
            return (ToggleButtonWidget) new ToggleButtonWidget(0, 0, 12, 12, showChannels::getValue, cd -> {
                showChannels.setValue(!showChannels.getValue());
                mainGroup.setVisible(showChannels.isFalse());
                channelsGroup.setVisible(showChannels.isTrue());
                requestUpdate();
            }).setTexture(
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0, 1, 0.5),
                            GuiTextures.BUTTON_LIST),
                    new GuiTextureGroup(GuiTextures.TOGGLE_BUTTON_BACK.getSubTexture(0, 0.5, 1, 0.5),
                            GuiTextures.BUTTON_LIST))
                    .setHoverTooltips("cover.ender_fluid_link.tooltip.list_button");
        }

        @Contract("_ -> new")
        private @NotNull Widget createToggleButtonForPrivacy(int currentX) {
            // return new EnumSelectorWidget<>(currentX, 0, WIDGET_BOARD, WIDGET_BOARD, Permissions.values(),
            // cover.permission, cover::setPermission);
            return null;
        }

        private ColorBlockWidget createColorBlockWidget(int currentX) {
            return new ColorBlockWidget(currentX, 0, WIDGET_BOARD, WIDGET_BOARD).setColorSupplier(cover::getColor);
        }

        private ConfirmTextInputWidget createConfirmTextInputWidget(int currentX) {
            int GROUP_X = 10;
            int textInputWidth = (GROUP_WIDTH - GROUP_X * 2) - currentX - WIDGET_BOARD - 2;
            return new ConfirmTextInputWidget(currentX, 0, textInputWidth, WIDGET_BOARD, cover.colorStr,
                    cover::setChannelName, text -> {
                        if (text == null || !COLOR_INPUT_PATTERN.matcher(text).matches()) {
                            return VirtualTank.DEFAULT_COLOR;
                        }
                        return text;
                    }, text -> {
                        if (text.length() < 8) {
                            text += "F".repeat(8 - text.length());
                        }
                        return text;
                    }).setTooltip("cover.ender_fluid_link.tooltip.channel_name");
        }

        @Contract(" -> new")
        private @NotNull ToggleButtonWidget createWorkingEnabledButton() {
            return new ToggleButtonWidget(116, 82, WIDGET_BOARD, WIDGET_BOARD, GuiTextures.BUTTON_POWER,
                    cover::isWorkingEnabled, cover::setWorkingEnabled);
        }

        private void addEnumSelectorWidgets() {
            mainGroup.addWidget(new EnumSelectorWidget<>(146, 82, WIDGET_BOARD, WIDGET_BOARD, List.of(IO.IN, IO.OUT),
                    cover.io, cover::setIo));
            mainGroup.addWidget(new EnumSelectorWidget<>(146, 107, WIDGET_BOARD, WIDGET_BOARD, ManualIOMode.VALUES,
                    cover.manualIOMode, cover::setManualIOMode)
                    .setHoverTooltips("cover.universal.manual_import_export.mode.description"));
        }

        private void addChannelWidgets(List<? extends VirtualEntry> entries) {
            channelsGroup.clearAllWidgets();
            int y = 1;
            SelectableWidgetGroup selectedWidget = null;
            for (var entry : entries.stream().sorted(Comparator.comparing(VirtualEntry::getColorStr)).toList()) {
                SelectableWidgetGroup channelWidget = createChannelWidget(entry, 10, y);
                if (cover.getChannelName(entry).equals(cover.getChannelName())) {
                    selectedWidget = channelWidget;
                }
                channelsGroup.addWidget(channelWidget);
                y += 22;
            }
            channelsGroup.setSelected(selectedWidget);
            if (selectedWidget != null) selectedWidget.onSelected();
            channelsGroup.setClientSideWidget();
        }

        private @NotNull SelectableWidgetGroup createChannelWidget(@NotNull VirtualEntry entry, int x, int y) {
            int currentX = 0;
            int MARGIN = 2;
            int availableWidth = TOTAL_WIDTH - (BUTTON_SIZE + MARGIN) * 3;

            final MutableBoolean canSelect = new MutableBoolean(false);
            var des = entry.getDescription();
            TextBoxWidget textBoxWidget = new TextBoxWidget(BUTTON_SIZE + MARGIN,
                    !des.isEmpty() ? 0 : 4, availableWidth, List.of(entry.getColorStr())).setCenter(true);
            SelectableWidgetGroup channelGroup = new SelectableWidgetGroup(x, y, TOTAL_WIDTH, BUTTON_SIZE) {

                @Override
                public boolean allowSelected(double mouseX, double mouseY, int button) {
                    return canSelect.getValue() && super.allowSelected(mouseX, mouseY, button);
                }
            };
            channelGroup.setOnSelected(group -> {
                if (cover.getChannelName().equals(cover.getChannelName(entry))) return;
                writeClientAction(0, buffer -> {
                    // send new channel name to server
                    String newChannelColorStr = entry.getColorStr();
                    buffer.writeUtf(newChannelColorStr);
                });
                playButtonClickSound();
            }).setSelectedTexture(1, -1);

            // Color block
            ColorBlockWidget colorBlockWidget = new ColorBlockWidget(currentX, 0, BUTTON_SIZE, BUTTON_SIZE)
                    .setCurrentColor(VirtualEntry.parseColor(entry.getColorStr()));
            channelGroup.addWidget(colorBlockWidget);
            currentX += BUTTON_SIZE + MARGIN;

            // Text box
            channelGroup.addWidget(textBoxWidget);
            currentX += availableWidth + MARGIN;
            if (!des.isEmpty()) {
                var desText = new TextTexture(ChatFormatting.DARK_GRAY + des).setDropShadow(false);
                desText.setType(TextTexture.TextType.ROLL).setRollSpeed(0.7f);
                channelGroup.addWidget(new ImageWidget(BUTTON_SIZE + MARGIN, 10, availableWidth, 8, desText));
            }

            // Slot
            // Widget slotWidget = cover.createVirtualEntryWidget(entry, currentX, 0, BUTTON_SIZE, BUTTON_SIZE, false);
            // channelGroup.addWidget(slotWidget);
            currentX += BUTTON_SIZE + MARGIN;

            // Clear Description button
            channelGroup.addWidget(
                    new ButtonWidget(currentX, 0, BUTTON_SIZE, BUTTON_SIZE, GuiTextures.BUTTON_CLEAR_GRID, press -> {
                        writeClientAction(200, buffer -> buffer.writeUtf(cover.getChannelName(entry)));
                        requestUpdate();
                    }) {

                        @Override
                        public boolean isMouseOverElement(double mouseX, double mouseY) {
                            var isOver = super.isMouseOverElement(mouseX, mouseY);
                            if (canSelect.getValue() == isOver) canSelect.setValue(!isOver);
                            return isOver;
                        }
                    }.appendHoverTooltips("cover.ender_fluid_link.tooltip.clear_button"));

            return channelGroup;
        }

        private void requestUpdate() {
            writeClientAction(100, buffer -> buffer.writeBoolean(showChannels.isTrue()));
        }

        @Override
        public void handleClientAction(int id, FriendlyByteBuf buffer) {
            super.handleClientAction(id, buffer);
            if (id == 0) {
                String newChannelColorStr = buffer.readUtf();
                cover.setChannelName(newChannelColorStr);
            } else if (id == 100) {
                if (!buffer.readBoolean()) return;
                var entries = VirtualEnderRegistry.getInstance().getEntryNames(cover.getOwner(), cover.getEntryType())
                        .stream().map(name -> VirtualEnderRegistry.getInstance().getEntry(cover.getOwner(),
                                cover.getEntryType(), name))
                        .sorted(Comparator.comparing(VirtualEntry::getColorStr));
                writeUpdateInfo(101, buf -> {
                    var list = entries.toList();
                    buf.writeVarInt(list.size());
                    for (var entry : list) {
                        buf.writeNbt(entry.serializeNBT());
                    }
                });
            } else if (id == 200) {
                String channelName = buffer.readUtf();
                VirtualEnderRegistry.getInstance().getEntry(cover.getOwner(), cover.getEntryType(), channelName)
                        .setDescription("");
            }
        }

        @Override
        public void readUpdateInfo(int id, FriendlyByteBuf buffer) {
            super.readUpdateInfo(id, buffer);
            if (id == 101) {
                int size = buffer.readVarInt();
                List<VirtualEntry> entries = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    VirtualEntry entry = cover.getEntryType().createInstance();
                    entry.deserializeNBT(Objects.requireNonNull(buffer.readNbt()));
                    entries.add(entry);
                }
                addChannelWidgets(entries);
            }
        }
    }

    private class VirtualEntryListAdapter implements IByteBufAdapter<List<VirtualEntry>> {

        @Override
        public List<VirtualEntry> deserialize(FriendlyByteBuf buffer) {
            List<VirtualEntry> list = new ArrayList<>();
            int size = buffer.readInt();
            for (int i = 0; i < size; i++) {
                VirtualEntry entry = getEntryType().createInstance();
                entry.deserializeNBT(buffer.readNbt());
                list.add(entry);
            }
            return list;
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, List<VirtualEntry> list) {
            buffer.writeInt(list.size());
            for (VirtualEntry entry : list) {
                buffer.writeNbt(entry.serializeNBT());
            }
        }

        @Override
        public boolean areEqual(@NotNull List<VirtualEntry> t1, @NotNull List<VirtualEntry> t2) {
            if (t1.size() != t2.size()) return false;
            return t1.equals(t2);
        }
    }
}
