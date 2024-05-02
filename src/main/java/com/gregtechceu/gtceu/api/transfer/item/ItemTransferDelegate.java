package com.gregtechceu.gtceu.api.transfer.item;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.NotNull;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ItemTransferDelegate implements IItemTransfer {
    public IItemTransfer delegate;

    public ItemTransferDelegate(IItemTransfer delegate) {
        this.delegate = delegate;
    }

    protected void setDelegate(IItemTransfer delegate) {
        this.delegate = delegate;
    }


    //////////////////////////////////////
    //******    OVERRIDE THESE    ******//
    //////////////////////////////////////

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    @NotNull
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        return delegate.insertItem(slot, stack, simulate, notifyChanges);
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        return delegate.extractItem(slot, amount, simulate, notifyChanges);
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }

    @Override
    @ApiStatus.Internal
    @NotNull
    public Object createSnapshot() {
        return delegate.createSnapshot();
    }

    @Override
    @ApiStatus.Internal
    public void restoreFromSnapshot(Object snapshot) {
        delegate.restoreFromSnapshot(snapshot);
    }


    @Override
    public void onContentsChanged() {
        delegate.onContentsChanged();
    }
}
