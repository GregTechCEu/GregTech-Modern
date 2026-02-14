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
import com.gregtechceu.gtceu.api.mui.value.BoolValue;
import com.gregtechceu.gtceu.api.mui.value.sync.DynamicLinkedSyncHandler;
import com.gregtechceu.gtceu.api.mui.value.sync.GenericListSyncHandler;
import com.gregtechceu.gtceu.api.mui.widgets.ButtonWidget;
import com.gregtechceu.gtceu.api.mui.widgets.CycleButtonWidget;
import com.gregtechceu.gtceu.api.sync_system.annotations.RerenderOnChanged;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IKey;
import com.gregtechceu.gtceu.api.mui.base.widget.IWidget;
import com.gregtechceu.gtceu.api.mui.drawable.Rectangle;
import com.gregtechceu.gtceu.api.mui.drawable.UITexture;
import com.gregtechceu.gtceu.api.mui.factory.SidedPosGuiData;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.MouseData;
import com.gregtechceu.gtceu.api.mui.value.sync.BooleanSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.EnumSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.GenericSyncValue;
import com.gregtechceu.gtceu.api.mui.value.sync.PanelSyncManager;
import com.gregtechceu.gtceu.api.mui.value.sync.StringSyncValue;
import com.gregtechceu.gtceu.api.mui.widget.EmptyWidget;
import com.gregtechceu.gtceu.api.mui.widget.ParentWidget;
import com.gregtechceu.gtceu.api.mui.widgets.DynamicSyncedWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ListWidget;
import com.gregtechceu.gtceu.api.mui.widgets.ToggleButton;
import com.gregtechceu.gtceu.api.mui.widgets.layout.Flow;
import com.gregtechceu.gtceu.api.mui.widgets.textfield.TextFieldWidget;
import com.gregtechceu.gtceu.client.mui.screen.UISettings;
import com.gregtechceu.gtceu.common.cover.data.ManualIOMode;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.serialization.network.IByteBufAdapter;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.IntSupplier;
import java.util.regex.Pattern;

@SuppressWarnings("SameParameterValue")
public abstract class AbstractEnderLinkCover<T extends VirtualEntry> extends CoverBehavior
                                            implements IMuiCover, IControllable {

    public static final Pattern COLOR_INPUT_PATTERN = Pattern.compile("^[0-9a-fA-F]{0,8}$");

    protected final ConditionalSubscriptionHandler subscriptionHandler;

    @Getter
    @SaveField
    @SyncToClient
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
    protected ManualIOMode manualIOMode = ManualIOMode.DISABLED;
    @Getter
    @SaveField
    @SyncToClient
    @RerenderOnChanged
    protected IO io = IO.OUT;
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
    public ParentWidget<?> createCoverUI(SidedPosGuiData data, PanelSyncManager syncManager, UISettings settings) {
        var isChannelListActive = new BooleanSyncValue(this::isChannelListActive, this::setChannelListActive);
        syncManager.syncValue("CLA", isChannelListActive);

        var entries = new GenericListSyncHandler.Builder<VirtualEntry>()
                .getter(this::getVirtualEntries)
                .setter(this::setVirtualEntries)
                .adapter(new VirtualEntryAdapter())
                .build();
        syncManager.syncValue("entries", entries);

        return Flow.column()
                .child(new ToggleButton().value(BoolValue.wrap(isChannelListActive))
                        .overlay(GTGuiTextures.MORE)
                        .tooltip(t -> t
                                .addLine(Component.translatable("cover.ender_fluid_link.tooltip.list_button")))
                        .marginLeft(4)
                        .size(16, 16))
                .child(createChannelNameRow(syncManager).setEnabledIf(f -> !isChannelListActive.getBoolValue()))
                .child(createDescriptionField().setEnabledIf(f -> !isChannelListActive.getBoolValue()))
                .child(createSettingsRow().setEnabledIf(f -> !isChannelListActive.getBoolValue()))
                .child(createChannelList(entries).setEnabledIf(f -> isChannelListActive.getBoolValue()))
                .rightRel(0.5F)
                .top(3)
                .childPadding(3)
                .collapseDisabledChild()
                .coverChildren();
    }

    public DynamicSyncedWidget<?> createChannelList(GenericListSyncHandler<VirtualEntry> entriesSyncer) {
        DynamicLinkedSyncHandler<GenericListSyncHandler<VirtualEntry>> dynamicLinkedSyncHandler = new DynamicLinkedSyncHandler<>(entriesSyncer)
                .widgetProvider((manager, entriesListSyncer) -> {
            if (entriesListSyncer == null || entriesListSyncer.getValue() == null) return new EmptyWidget();
            ListWidget<IWidget, ?> list = new ListWidget<>();
            List<VirtualEntry> entryList = entriesListSyncer.getValue();
            for (var i=0;i<entryList.size();i++) {
                var entry = entryList.get(i);
                list.child(createVirtualEntryRow(manager, entry, i));
            }
            return list.childSeparator(GTGuiTextures.SEPERATOR_SIMPLE.asIcon().size(116, 5).margin(12, 0))
                    .size(162, 58);
        });
        return new DynamicSyncedWidget<>().syncHandler(dynamicLinkedSyncHandler).size(162, 58);
    }

    public Flow createVirtualEntryRow(PanelSyncManager syncManager, VirtualEntry entry, int index) {
        return Flow.row()
                .child(createColorBlock(entry::getColor, 18).asWidget()
                        .tooltip(t -> t.addLine(entry.getColorStr()))
                        .size(18, 18))
                .child(IKey.str(entry.getDescription()).asWidget().size(92, 12))
                .child(createVirtualEntryWidget(syncManager, entry, 18, 18, index))
                .child(new ButtonWidget<>().overlay(GTGuiTextures.BUTTON_CROSS)
                        .onMousePressed((x, y, button) -> {
                            MouseData mouseData = MouseData.create(button);
                            if (mouseData.mouseButton() == 1) {
                                VirtualEnderRegistry.getInstance()
                                        .getEntry(getOwner(), getEntryType(), entry.getColorStr()).setDescription("");
                                return true;
                            }
                            return false;
                        }))
                .alignX(0F)
                .childPadding(3)
                .coverChildren();
    }

    public Flow createChannelNameRow(PanelSyncManager syncManager) {
        return Flow.row()
                .child(createColorBlock(this::getColor, 18).asWidget().marginRight(3))
                .child(new TextFieldWidget().value(new StringSyncValue(this::getColorStr, this::setChannelName))
                        .setPattern(COLOR_INPUT_PATTERN)
                        .setValidator(String::toUpperCase)
                        .hintText(Component.translatable("cover.ender_link.channel_name"))
                        .expanded()
                        .height(16))
                .child(createVirtualEntryWidget(syncManager, this.getEntry(), 18, 18, -1))
                .size(162, 18);
    }

    public Flow createDescriptionField() {
        return Flow.row()
                .child(new TextFieldWidget()
                        .value(new StringSyncValue(() -> {
                            if(getEntry() == null) return "null";
                            return getEntry().getDescription();
                        }, (newVal) -> {
                            if(getEntry() != null) getEntry().setDescription(newVal);
                        }))
                        .hintText(Component.translatable("cover.ender_link.channel_description"))
                        .widthRel(1F)
                        .height(16))
                .widthRel(1F)
                .coverChildrenHeight();
    }

    public Flow createSettingsRow() {
        return Flow.row()
                // Power button
                .child(new ToggleButton().value(new BooleanSyncValue(this::isWorkingEnabled, this::setWorkingEnabled))
                        // TODO: once the branch that has power overlays defined in GTGuiTextures is merged, we need to
                        // TODO: replace this temporary usage with those. or keep them. they are pretty cool
                        .overlay(false, GTGuiTextures.PLAY)
                        .overlay(true, GTGuiTextures.PLAY))
                // Import / Export button
                .child(new CycleButtonWidget()
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
                .child(new CycleButtonWidget()
                        .stateCount(2)
                        .stateOverlay(Permissions.PUBLIC, Permissions.PUBLIC.icon)
                        .stateOverlay(Permissions.PRIVATE, Permissions.PRIVATE.icon)
                        .tooltip(0, t -> t.addLine(IKey.lang(Permissions.PUBLIC.tooltip + ".0"))
                                .addLine(IKey.lang(Permissions.PUBLIC.tooltip + ".1")))
                        .tooltip(1, t -> t.addLine(IKey.lang(Permissions.PRIVATE.tooltip)))
                        .value(new EnumSyncValue<>(Permissions.class, this::getPermission,
                                this::setPermission)))
                // Manual IO button
                .child(new CycleButtonWidget()
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
                (context, x, y, w, h, widgetTheme) -> new Rectangle().color(Color.BLACK.main)
                        .draw(context, x, y, size, size, widgetTheme),
                // Colored block
                (context, x, y, w, h, widgetTheme) -> new Rectangle().color(colorSupplier.getAsInt())
                        .draw(context, x + 1, y + 1, size - 2, size - 2, widgetTheme));
    }

    public List<VirtualEntry> getVirtualEntries() {
        List<VirtualEntry> entries = new ArrayList<>();
        for (String entryName : VirtualEnderRegistry.getInstance().getEntryNames(getOwner(), getEntryType())) {
            entries.add(VirtualEnderRegistry.getInstance().getEntry(getOwner(), getEntryType(), entryName));
        }
        return entries;
    }

    public void setVirtualEntries(List<VirtualEntry> entries) {}

    private void reverseIO(IO io) {
        setIo(IO.values()[io.ordinal() % 2]);
    }

    public void setIo(IO io) {
        if (io == IO.IN || io == IO.OUT) {
            this.io = io;
            syncDataHolder.markClientSyncFieldDirty("io");
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
        syncDataHolder.markClientSyncFieldDirty("colorStr");
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
        syncDataHolder.markClientSyncFieldDirty("permission");

        setVirtualEntry();
    }

    protected void setVirtualEntry() {
        setEntry(VirtualEnderRegistry.getInstance().getOrCreateEntry(getOwner(), getEntryType(), getChannelName()));
        getEntry().setColor(this.colorStr);
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
        subscriptionHandler.updateSubscription();
    }

    protected abstract void transfer();

    protected void setManualIOMode(ManualIOMode manualIOMode) {
        this.manualIOMode = manualIOMode;
        syncDataHolder.markClientSyncFieldDirty("manualIOMode");
        subscriptionHandler.updateSubscription();
    }

    @Nullable
    protected FilterHandler<?, ?> getFilterHandler() {
        return null;
    }

    /**
     * All syncers registered through this method MUST use the {@link PanelSyncManager#getOrCreateSyncHandler} method
     * for applying a syncer to a widget because it gets placed into a {@link DynamicSyncedWidget}.
     *
     * @return A widget to represent the entry type for this cover
     */
    protected abstract IWidget createVirtualEntryWidget(PanelSyncManager manager, VirtualEntry entry, int w, int h, int index);

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

    private class VirtualEntryAdapter implements IByteBufAdapter<VirtualEntry> {

        @Override
        public VirtualEntry deserialize(FriendlyByteBuf buffer) {
            VirtualEntry entry = getEntryType().createInstance();
            entry.deserializeNBT(buffer.readNbt());
            return entry;
        }

        @Override
        public void serialize(FriendlyByteBuf buffer, VirtualEntry entry) {
            buffer.writeNbt(entry.serializeNBT());
        }

        @Override
        public boolean areEqual(@NotNull VirtualEntry t1, @NotNull VirtualEntry t2) {
            return t1.equals(t2);
        }
    }
}
