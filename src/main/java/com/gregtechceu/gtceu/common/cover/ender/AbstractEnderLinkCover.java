package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.ICoverable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.cover.CoverBehavior;
import com.gregtechceu.gtceu.api.cover.CoverDefinition;
import com.gregtechceu.gtceu.api.cover.IMuiCover;
import com.gregtechceu.gtceu.api.cover.filter.FilterHandler;
import com.gregtechceu.gtceu.api.machine.ConditionalSubscriptionHandler;
import com.gregtechceu.gtceu.api.machine.MachineCoverContainer;
import com.gregtechceu.gtceu.api.misc.virtualregistry.EntryTypes;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEnderRegistry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.IKey;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.Rectangle;
import brachy.modularui.factory.GuiData;
import brachy.modularui.factory.SidedPosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.MouseData;
import brachy.modularui.utils.serialization.network.IByteBufAdapter;
import brachy.modularui.value.sync.*;
import brachy.modularui.widget.EmptyWidget;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.*;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractEnderLinkCover<T extends VirtualEntry> extends CoverBehavior
                                            implements IMuiCover, IControllable {

    public static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("([^0-9a-fA-F])");

    protected final ConditionalSubscriptionHandler subscriptionHandler;

    @SaveField
    @SyncToClient
    @Getter(value = AccessLevel.PROTECTED)
    protected String colorStr = VirtualEntry.DEFAULT_COLOR;
    @Getter
    @SaveField
    @SyncToClient
    protected Permissions permission = Permissions.PUBLIC;
    @SaveField
    @Getter
    protected boolean isWorkingEnabled = true;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected IO io = IO.OUT;

    public AbstractEnderLinkCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
        subscriptionHandler = new ConditionalSubscriptionHandler(coverHolder, this::update, this::isWorkingEnabled);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        subscriptionHandler.initialize(coverHolder.getLevel());
    }

    @Override
    public abstract boolean canAttach();

    @Override
    public void onUnload() {
        super.onUnload();
        subscriptionHandler.unsubscribe();
        if (!isRemote()) {
            VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel())
                    .tryDeleteEntry(getOwner(), getEntryType(), getColorStr());
        }
    }

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
            syncDataHolder.markClientSyncFieldDirty("io");
            subscriptionHandler.updateSubscription();
        }
    }

    public @Nullable UUID getOwner() {
        if (permission == Permissions.PRIVATE && coverHolder instanceof MachineCoverContainer mcc) {
            var owner = mcc.getMachine().getOwner();
            return owner != null ? owner.getPlayerUUID() : null;
        }
        if (permission == Permissions.PROTECTED && coverHolder instanceof MachineCoverContainer mcc) {
            var owner = mcc.getMachine().getOwner();
            return owner != null ? owner.getUUID() : null;
        }
        return null;
    }

    protected @Nullable abstract VirtualEntry getEntry();

    protected abstract void setEntry(VirtualEntry entry);

    protected abstract EntryTypes<T> getEntryType();

    protected abstract void transfer();

    /**
     * All syncers registered through this method MUST use the {@link PanelSyncManager#getOrCreateSyncHandler} method
     * for applying a syncer to a widget because it gets placed into a {@link DynamicSyncedWidget}.
     *
     * @return A widget to represent the entry type for this cover
     */
    protected abstract IWidget createVirtualEntryWidget(PanelSyncManager manager, VirtualEntry entry, int w, int h,
                                                        int idx);

    @Nullable
    protected FilterHandler<?, ?> getFilterHandler() {
        return null;
    }

    protected void setColorStr(String str) {
        if (isRemote()) return;
        if (str.length() != 8) str = str.concat("F".repeat(8 - str.length()));
        VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel()).tryDeleteEntry(getOwner(), getEntryType(),
                getColorStr());
        this.colorStr = str;
        syncDataHolder.markClientSyncFieldDirty("colorStr");
        setVirtualEntry();
    }

    protected void setPermission(Permissions permission) {
        if (isRemote()) return;
        VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel()).tryDeleteEntry(getOwner(), getEntryType(),
                getColorStr());
        this.permission = permission;
        syncDataHolder.markClientSyncFieldDirty("permission");
        setVirtualEntry();
    }

    protected void setVirtualEntry() {
        setEntry(VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel()).getOrCreateEntry(getOwner(),
                getEntryType(), getColorStr()));
        Objects.requireNonNull(getEntry()).setColor(this.colorStr);
        subscriptionHandler.updateSubscription();
    }

    protected void update() {
        long timer = coverHolder.getOffsetTimer();
        if (timer % 5 != 0) return;

        if (isWorkingEnabled() && !isRemote()) {
            var entry = VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel()).getOrCreateEntry(getOwner(),
                    getEntryType(), getColorStr());
            if (!entry.getColorStr().equals(this.colorStr)) {
                entry.setColor(this.colorStr);
            }
            if (!Objects.equals(getEntry(), entry)) {
                setEntry(entry);
            }
            transfer();
        }

        subscriptionHandler.updateSubscription();
    }

    protected int getColor() {
        return VirtualEntry.parseColor(this.colorStr);
    }

    private String getDescription() {
        return getEntry() == null ? "null" : getEntry().getDescription();
    }

    private void setDescription(String description) {
        if (getEntry() != null) getEntry().setDescription(description);
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putString("colorStr", colorStr);
        tag.putInt("permission", getPermission().ordinal());
        tag.putInt("io", getIo().ordinal());
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setColorStr(tag.getString("colorStr"));
        setPermission(Permissions.values()[tag.getInt("permission")]);
        setIo(IO.values()[tag.getInt("io")]);
        super.pasteConfig(player, tag);
    }

    private List<VirtualEntry> getVirtualEntries() {
        return VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel()).getEntries(getOwner(), getEntryType())
                .values().stream().toList();
    }

    protected enum Permissions {

        PUBLIC("cover.ender_link.public.tooltip"),
        PROTECTED("cover.ender_link.protected.tooltip"),
        PRIVATE("cover.ender_link.private.tooltip");

        @Getter
        private final String tooltip;

        Permissions(String tooltip) {
            this.tooltip = tooltip;
        }
    }

    @Override
    public void createCoverUIRows(Flow column, SidedPosGuiData data, PanelSyncManager syncManager,
                                  UISettings settings) {
        var channelManager = syncManager.syncedPanel("channelManager", true,
                (sm, sh) -> createChannelManagerPanel(data, sm, settings));

        var colorSyncer = new IntSyncValue(this::getColor);
        EnumSyncValue<IO> ioSync = new EnumSyncValue<>(IO.class, this::getIo, this::setIo);

        syncManager.syncValue("io", ioSync);
        syncManager.syncValue("color", colorSyncer);

        var currentEntry = GenericSyncValue.builder(VirtualEntry.class)
                .getter(this::getEntry)
                .adapter(new VirtualEntryAdapter()).build();
        syncManager.syncValue("currentEntry", currentEntry);

        DynamicLinkedSyncHandler<GenericSyncValue<VirtualEntry>> dynamicLinkedSyncHandler = new DynamicLinkedSyncHandler<>(
                currentEntry)
                .widgetProvider((manager, entriesListSyncer) -> createVirtualEntryWidget(manager,
                        entriesListSyncer.getValue(), 18, 18, 0));

        column.child(coverUIRow()
                .child(createColorBlock(colorSyncer::getIntValue, 18).asWidget().size(18))
                .child(new CycleButtonWidget()
                        .stateCount(3)
                        .stateOverlay(Permissions.PUBLIC, GTGuiTextures.PRIVATE_MODE_BUTTON[0])
                        .stateOverlay(Permissions.PROTECTED, GTGuiTextures.PRIVATE_MODE_BUTTON[0])
                        .stateOverlay(Permissions.PRIVATE, GTGuiTextures.PRIVATE_MODE_BUTTON[1])
                        .tooltip(0, t -> t.addLine(IKey.lang(Permissions.PUBLIC.tooltip)))
                        .tooltip(1, t -> t.addLine(IKey.lang(Permissions.PROTECTED.tooltip)))
                        .tooltip(2, t -> t.addLine(IKey.lang(Permissions.PRIVATE.tooltip)))
                        .value(new EnumSyncValue<>(Permissions.class, this::getPermission,
                                this::setPermission)))
                .child(new TextFieldWidget()
                        .value(new StringSyncValue(this::getColorStr, this::setColorStr))
                        .setMaxLength(8)
                        .setValidator(str -> COLOR_INPUT_PATTERN.matcher(str).replaceAll(""))
                        .addTooltipLine(IKey.lang(Component.translatable("cover.ender_link.tooltip.channel_name"))))
                .child(new DynamicSyncedWidget<>().syncHandler(dynamicLinkedSyncHandler))
                .child(new ButtonWidget<>().onMousePressed((x, y, b) -> {
                    channelManager.openPanel();
                    return true;
                }).posRel(Alignment.CenterRight).tooltip(new RichTooltip()
                        .addLine(IKey.lang(Component.translatable("cover.ender_link.tooltip.list_button"))))));

        column.child(coverUIRow().child(new TextFieldWidget()
                .setMaxLength(32)
                .widthRel(1f)
                .addTooltipLine(IKey.lang(Component.translatable("cover.ender_link.tooltip.channel_description")))
                .value(new StringSyncValue(this::getDescription, this::setDescription))));

        Flow bottomRow = coverUIRow();
        bottomRow.child(GTMuiWidgets.createPowerButton(this));
        bottomRow.child(GTMuiWidgets.createIOCycleButton(ioSync, false));
        if (getFilterHandler() != null)
            GTMuiWidgets.createFilterRow(bottomRow, getFilterHandler(), data, syncManager, settings);
        column.child(bottomRow);
    }

    protected ParentWidget<?> getChannelStatusRowShort(PanelSyncManager syncManager, VirtualEntry entry, int idx) {
        TextWidget<?> str;
        if (entry.getDescription().isBlank()) {
            str = IKey.lang(Component.translatable("cover.ender_link.description_empty")).asWidget().size(98, 12)
                    .color(Color.GREY.darker(1));
        } else {
            str = IKey.str(entry.getDescription()).asWidget().size(98, 12);
        }
        return coverUIRow()
                .child(createColorBlock(entry::getColor, 18).asWidget()
                        .tooltip(t -> t.addLine(entry.getColorStr()))
                        .size(18, 18))
                .child(str)
                .child(createVirtualEntryWidget(syncManager, entry, 18, 18, idx))
                .child(new ButtonWidget<>().overlay(GTGuiTextures.BUTTON_CROSS).onMousePressed((x, y, button) -> {
                    MouseData mouseData = MouseData.create(button);
                    if (mouseData.mouseButton() == 1) {
                        syncManager.callSyncedAction("deleteEntry",
                                buffer -> buffer.writeCharSequence(entry.getColorStr(), StandardCharsets.UTF_8));
                        return true;
                    }
                    return false;
                }).posRel(Alignment.CenterRight));
    }

    public IDrawable createColorBlock(IntSupplier colorSupplier, int size) {
        return IDrawable.of(
                // Border
                (context, x, y, w, h, widgetTheme) -> new Rectangle().color(Color.BLACK.main)
                        .draw(context, x, y, size, size, widgetTheme),
                // Colored block
                (context, x, y, w, h, widgetTheme) -> new Rectangle().color(colorSupplier.getAsInt())
                        .draw(context, x + 1, y + 1, size - 2, size - 2, widgetTheme));
    }

    protected ModularPanel<?> createChannelManagerPanel(GuiData data, PanelSyncManager syncManager,
                                                        UISettings settings) {
        var panel = new Dialog<>("channel_manager")
                .disablePanelsBelow(false)
                .draggable(true)
                .closeOnOutOfBoundsClick(true)
                .child(GTMuiWidgets.createTitleBar(getAttachItem(), 176, GTGuiTextures.BACKGROUND));

        var entries = new GenericListSyncHandler.Builder<VirtualEntry>()
                .getter(this::getVirtualEntries)
                .adapter(new VirtualEntryAdapter())
                .build();
        syncManager.syncValue("entries", entries);

        DynamicLinkedSyncHandler<GenericListSyncHandler<VirtualEntry>> dynamicLinkedSyncHandler = new DynamicLinkedSyncHandler<>(
                entries)
                .widgetProvider((manager, entriesListSyncer) -> {
                    if (entriesListSyncer == null || entriesListSyncer.getValue() == null) return new EmptyWidget();
                    ListWidget<IWidget, ?> list = new ListWidget<>();
                    List<VirtualEntry> entryList = entriesListSyncer.getValue();
                    for (int i = 0; i < entryList.size(); i++) {
                        list.child(getChannelStatusRowShort(manager, entryList.get(i), i));
                    }
                    return list.childSeparator(GTGuiTextures.SEPERATOR_SIMPLE.asIcon().size(116, 5).margin(12, 0))
                            .size(162, 154);
                });

        syncManager.registerServerSyncedAction("deleteEntry", (packet) -> {
            var colorString = packet.readCharSequence(8, StandardCharsets.UTF_8).toString();
            VirtualEntry entry = VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel()).getEntry(getOwner(),
                    getEntryType(), colorString);
            if (entry != null) {
                entry.setDescription("");
                VirtualEnderRegistry.get((ServerLevel) coverHolder.getLevel()).tryDeleteEntry(getOwner(),
                        getEntryType(), colorString);
            }
        });

        panel.child(new DynamicSyncedWidget<>()
                .syncHandler(dynamicLinkedSyncHandler)
                .top(7).margin(7, 0)
                .widthRel(1.0f).coverChildrenHeight());

        return panel;
    }

    private class VirtualEntryAdapter implements IByteBufAdapter<VirtualEntry> {

        @Override
        public VirtualEntry deserialize(FriendlyByteBuf buffer) {
            VirtualEntry entry = getEntryType().createInstance();
            var nbt = buffer.readNbt();
            if (nbt == null) return entry;
            entry.deserializeNBT(nbt);
            return entry;
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, VirtualEntry entry) {
            buffer.writeNbt(entry.serializeNBT());
        }

        @Override
        public boolean areEqual(VirtualEntry t1, VirtualEntry t2) {
            return t1.getColor() == t2.getColor() && t1.getDescription().equals(t2.getDescription());
        }
    }
}
