package com.gregtechceu.gtceu.common.cover.filter;

import com.gregtechceu.gtceu.api.blockentity.IDirtyNotifiable;
import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.lowdragmc.lowdraglib.misc.ItemStackTransfer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseFilterContainer extends ItemStackTransfer {

    private int maxTransferSize = 1;
    private int transferSize;
    private @Nullable ItemFilter currentFilter;
    private @Nullable Runnable onFilterInstanceChange;
    private final IDirtyNotifiable dirtyNotifiable;

    protected BaseFilterContainer(IDirtyNotifiable dirtyNotifiable) {
        super();
        this.dirtyNotifiable = dirtyNotifiable;
    }

    public boolean test(ItemStack toTest) {
        return !hasFilter() || getFilter().test(toTest);
    }

    public boolean match(ItemStack toMatch) {
        if (!hasFilter())
            return true;

        return getFilter().test(toMatch);
    }

    public int getTransferLimit(ItemStack stack) {
        if (!hasFilter() || isBlacklistFilter()) {
            return getTransferSize();
        }
        return getFilter().testItemCount(stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    public void onFilterInstanceChange() {
        dirtyNotifiable.markAsDirty();
        if (onFilterInstanceChange != null) {
            onFilterInstanceChange.run();
        }
    }

    public void setOnFilterInstanceChange(@Nullable Runnable onFilterInstanceChange) {
        this.onFilterInstanceChange = onFilterInstanceChange;
    }

    public final @NotNull ItemStack getFilterStack() {
        return this.getStackInSlot(0);
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        if (ItemStack.isSameItemSameTags(stack, getFilterStack()))
            return;

        if (stack.isEmpty()) {
            setItemFilter(null);
        } else if (isItemValid(stack)) {
            setItemFilter(ItemFilter.loadFilter(stack));
        }

        super.setStackInSlot(slot, stack);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return isItemValid(stack);
    }

    protected abstract boolean isItemValid(ItemStack stack);

    protected abstract String getFilterName();

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!isItemValid(stack)) return stack;
        var remainder = super.insertItem(slot, stack, simulate);
        if (!simulate) setItemFilter(ItemFilter.loadFilter(stack));
        return remainder;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        var extracted = super.extractItem(slot, amount, simulate);
        if (!extracted.isEmpty()) {
            setFilter(null);
        }
        return extracted;
    }

    public final void setFilterStack(ItemStack stack) {
        setStackInSlot(0, stack);
    }

    public int getMaxTransferSize() {
        return !showGlobalTransferLimitSlider() && hasFilter() ? currentFilter.getMaxStackSize() :
                this.maxTransferSize;
    }

    public void setMaxTransferSize(int maxTransferSize) {
        this.maxTransferSize = Mth.clamp(maxTransferSize, 1, Integer.MAX_VALUE);
        this.transferSize = Mth.clamp(this.transferSize, 1, this.maxTransferSize);
        //if (hasFilter()) currentFilter.setMaxTransferSize(this.maxTransferSize);
    }

    public final boolean hasFilter() {
        return currentFilter != null;
    }

    public final @Nullable ItemFilter getFilter() {
        return currentFilter;
    }

    public final void setItemFilter(@Nullable ItemFilter newFilter) {
        this.currentFilter = newFilter;
        if (hasFilter()) {
            this.currentFilter.setOnUpdated($ -> this.dirtyNotifiable.markAsDirty());
            //this.currentFilter.setMaxTransferSize(this.maxTransferSize);
        }
        if (onFilterInstanceChange != null) {
            this.onFilterInstanceChange.run();
        }
    }

    public boolean showGlobalTransferLimitSlider() {
        return this.maxTransferSize > 0 && (!hasFilter()/* || getFilter().showGlobalTransferLimitSlider()*/);
    }

    public void setBlacklistFilter(boolean blacklistFilter) {
        if (hasFilter()) getFilter().setBlackList(blacklistFilter);
        onFilterInstanceChange();
    }

    public final boolean isBlacklistFilter() {
        return hasFilter() && getFilter().isBlackList();
    }

    public int getTransferSize() {
        if (!showGlobalTransferLimitSlider()) {
            return getMaxTransferSize();
        }
        return this.transferSize;
    }

    public int getTransferLimit(int slotIndex) {
        if (isBlacklistFilter() || !hasFilter()) {
            return getTransferSize();
        }
        return this.currentFilter.testItemCount(getStackInSlot(slotIndex));
    }

    public void setTransferSize(int transferSize) {
        this.transferSize = Mth.clamp(transferSize, 1, getMaxTransferSize());
        onFilterInstanceChange();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tagCompound = new CompoundTag();
        tagCompound.put("FilterInventory", super.serializeNBT());
        tagCompound.putInt("TransferStackSize", getTransferSize());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        super.deserializeNBT(nbt.getCompound("FilterInventory"));
        setItemFilter(ItemFilter.loadFilter(getFilterStack()));
        if (nbt.contains("TransferStackSize"))
            this.transferSize = nbt.getInt("TransferStackSize");
    }

    public void handleLegacyNBT(CompoundTag nbt) {
        // for filters as covers, the stack is set manually, and "FilterInventory" doesn't exist to be deserialized
        // also, ItemStackHandler's deserialization doesn't use setStackInSlot, so I have to do that manually here
        if (nbt.contains("FilterInventory")) {
            super.deserializeNBT(nbt.getCompound("FilterInventory"));
            setItemFilter(ItemFilter.loadFilter(getFilterStack()));
        }
    }

    /** Uses Cleanroom MUI */
    // TODO ui
    /*
    public IWidget initUI(ModularPanel main, PanelSyncManager manager) {
        PanelSyncHandler panel = manager.panel("filter_panel", main, (syncManager, syncHandler) -> {
            var filter = hasFilter() ? getFilter() : BaseFilter.ERROR_FILTER;
            filter.setMaxTransferSize(getMaxTransferSize());
            return filter.createPopupPanel(syncManager);
        });

        var filterButton = new ButtonWidget<>();
        filterButton.setEnabled(hasFilter());

        return new Row().coverChildrenHeight()
                .marginBottom(2).widthRel(1f)
                .child(new ItemSlot()
                        .slot(SyncHandlers.itemSlot(this, 0)
                                .filter(this::isItemValid)
                                .singletonSlotGroup(101)
                                .changeListener((newItem, onlyAmountChanged, client, init) -> {
                                    if (!isItemValid(newItem) && panel.isPanelOpen()) {
                                        panel.closePanel();
                                    }
                                }))
                        .size(18).marginRight(2)
                        .background(GTGuiTextures.SLOT, GTGuiTextures.FILTER_SLOT_OVERLAY.asIcon().size(16)))
                .child(filterButton
                        .background(GTGuiTextures.MC_BUTTON, GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .hoverBackground(GuiTextures.MC_BUTTON_HOVERED,
                                GTGuiTextures.FILTER_SETTINGS_OVERLAY.asIcon().size(16))
                        .setEnabledIf(w -> hasFilter())
                        .onMousePressed(i -> {
                            if (!panel.isPanelOpen()) {
                                panel.openPanel();
                            } else {
                                panel.closePanel();
                            }
                            Interactable.playButtonClickSound();
                            return true;
                        }))
                .child(IKey.dynamic(this::getFilterName)
                        .alignment(Alignment.CenterRight).asWidget()
                        .left(36).right(0).height(18));
    }
    */

    public void writeInitialSyncData(FriendlyByteBuf packetBuffer) {
        packetBuffer.writeItem(this.getFilterStack());
        packetBuffer.writeInt(this.maxTransferSize);
        packetBuffer.writeInt(this.transferSize);
    }

    public void readInitialSyncData(@NotNull FriendlyByteBuf packetBuffer) {
        var stack = packetBuffer.readItem();
        this.setFilterStack(stack);
        this.setMaxTransferSize(packetBuffer.readInt());
        this.setTransferSize(packetBuffer.readInt());
    }
}
