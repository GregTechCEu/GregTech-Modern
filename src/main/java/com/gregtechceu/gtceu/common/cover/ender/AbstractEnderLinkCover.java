package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IUICover;
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
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.machine.owner.IMachineOwner;

import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public abstract class AbstractEnderLinkCover<T extends VirtualEntry> extends CoverBehavior
                                            implements IUICover, IControllable {

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(AbstractEnderLinkCover.class,
            CoverBehavior.MANAGED_FIELD_HOLDER);
    public static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("[0-9a-fA-F]*");
    protected final ConditionalSubscriptionHandler subscriptionHandler;
    @Persisted
    @DescSynced
    protected String color = VirtualEntry.DEFAULT_COLOR;
    @Persisted
    @DescSynced
    protected String description = VirtualEntry.DEFAULT_COLOR;
    @Persisted
    protected IMachineOwner owner = null;
    @Getter
    @Setter
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

    @Override
    public abstract boolean canAttach();

    @Override
    public void onAttached(@NotNull ItemStack itemStack, @NotNull ServerPlayer player) {
        super.onAttached(itemStack, player);
        if (coverHolder instanceof MachineCoverContainer mcc) {
            this.owner = mcc.getMachine().getHolder().getOwner();
        }
    }

    protected final String getChannelName() {
        return identifier() + this.color;
    }

    protected abstract String identifier();

    protected void setChannelName(String name) {
        beforeChannelNameChanging(getChannelName());
        this.color = name;
    }

    protected void beforeChannelNameChanging(String oldChannelName) {
        var reg = VirtualEnderRegistry.getInstance();
        if (reg == null) return;
        reg.deleteEntryIf(getOwner(), getEntryType(), oldChannelName, VirtualEntry::canRemove);
    }

    protected abstract EntryTypes<T> getEntryType();

    protected abstract void updateEntry();

    public UUID getOwner() {
        return isPrivate ? owner.getPlayerUUID() : null;
    }

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 != 0) return;

        if (isWorkingEnabled()) {
            transfer();
        }
        updateEntry();

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
        }
    }

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        coverHolder.markDirty();
    }

    @Override
    public Widget createUIWidget() {
        updateEntry();
        final int GROUP_WIDTH = 176;
        final int WIDGET_HEIGHT = 20;
        final int SMALL_WIDGET_WIDTH = 20;
        final int GROUP_X = 10;
        final int GROUP_Y = 20;
        int channelGroupWidth = GROUP_WIDTH - GROUP_X * 2;
        int currentX = 0;

        final var group = new WidgetGroup(0, 0, 176, 137);
        final var channelGroup = new WidgetGroup(GROUP_X, GROUP_Y, channelGroupWidth, WIDGET_HEIGHT);
        group.addWidget(new LabelWidget(10, 5, getUITitle()));

        var toggleButtonWidget = new ToggleButtonWidget(currentX, 0, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT,
                GuiTextures.BUTTON_PUBLIC_PRIVATE, this::isPrivate, null);
        toggleButtonWidget.setOnPressCallback((clickData, isPrivate) -> {
            setPrivate(isPrivate);
            toggleButtonWidget.setHoverTooltips(isPrivate ?
                    "cover.ender_link.permission.public" : "cover.ender_link.permission.private");
        });
        channelGroup.addWidget(toggleButtonWidget);
        currentX += SMALL_WIDGET_WIDTH + 2;

        channelGroup.addWidget(new ColorBlockWidget(currentX, 0, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT)
                .setColorSupplier(this::getColor));
        currentX += SMALL_WIDGET_WIDTH + 2;

        int textInputWidth = channelGroupWidth - currentX - SMALL_WIDGET_WIDTH - 2;
        var confirmTextInputWidget = new ConfirmTextInputWidget(currentX, 0, textInputWidth, WIDGET_HEIGHT,
                this.color,
                text -> {
                    if (text != null && !text.isEmpty()) {
                        setChannelName(text);
                    }
                },
                text -> {
                    if (text == null || !COLOR_INPUT_PATTERN.matcher(text).matches() || text.length() > 8) {
                        return VirtualTank.DEFAULT_COLOR;
                    }
                    return text;
                }).setInputBoxTooltips(description);
        channelGroup.addWidget(confirmTextInputWidget);
        group.addWidget(channelGroup);

        group.addWidget(new ToggleButtonWidget(116, 82, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT,
                GuiTextures.BUTTON_POWER, this::isWorkingEnabled, this::setWorkingEnabled));
        group.addWidget(new EnumSelectorWidget<>(146, 82, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT,
                List.of(IO.IN, IO.OUT), io, this::setIo));
        group.addWidget(new EnumSelectorWidget<>(146, 107, SMALL_WIDGET_WIDTH, WIDGET_HEIGHT,
                ManualIOMode.VALUES, manualIOMode, this::setManualIOMode)
                .setHoverTooltips("cover.universal.manual_import_export.mode.description"));

        buildAdditionalUI(group);
        return group;
    }

    protected void buildAdditionalUI(WidgetGroup group) {}

    protected abstract String getUITitle();

    protected int getColor() {
        var colorString = this.color;
        colorString = String.format("%8s", colorString).replace(' ', '0');

        if (colorString.length() > 8) {
            colorString = colorString.substring(colorString.length() - 8);
        }

        int alpha = Integer.parseInt(colorString.substring(6, 8), 16);
        int red = Integer.parseInt(colorString.substring(0, 2), 16);
        int green = Integer.parseInt(colorString.substring(2, 4), 16);
        int blue = Integer.parseInt(colorString.substring(4, 6), 16);

        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }
}
