package com.gregtechceu.gtceu.api.misc;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class LimitingItemTransferProxy implements IItemTransfer {
    private final IItemTransfer delegate;

    @Getter @Setter
    private int remainingTransfer;

    public LimitingItemTransferProxy(IItemTransfer delegate, int transferLimit) {
        this.delegate = delegate;
        this.remainingTransfer = transferLimit;
    }

    @NotNull
    private ItemStack copyLimited(@NotNull ItemStack stack) {
        var limitedStack = stack.copy();
        limitedStack.setCount(Math.min(limitedStack.getCount(), remainingTransfer));

        return limitedStack;
    }

    /////////////////////////////////////
    //********    OVERRIDES    ********//
    /////////////////////////////////////


    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        var remainder = delegate.insertItem(slot, stack, simulate, notifyChanges);

        if (!simulate) {
            remainingTransfer -= copyLimited(stack).getCount() - (remainder.isEmpty() ? 0 : remainder.getCount());
        }

        return remainder;
    }

    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        var remainder = delegate.insertItem(slot, stack, simulate);

        if (!simulate) {
            remainingTransfer -= copyLimited(stack).getCount() - (remainder.isEmpty() ? 0 : remainder.getCount());
        }

        return remainder;
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        var extracted = delegate.extractItem(slot, Math.min(amount, remainingTransfer), simulate, notifyChanges);

        if (!simulate && !extracted.isEmpty()) {
            remainingTransfer -= extracted.getCount();
        }

        return extracted;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var extracted = delegate.extractItem(slot, Math.min(amount, remainingTransfer), simulate);

        if (!simulate && !extracted.isEmpty()) {
            remainingTransfer -= extracted.getCount();
        }

        return extracted;
    }

    ////////////////////////////////////////////    Delegated as is:    ////////////////////////////////////////////

    @Override
    public int getSlots() {
        return delegate.getSlots();
    }

    @Override
    @Nonnull
    public ItemStack getStackInSlot(int i) {
        return delegate.getStackInSlot(i);
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        delegate.setStackInSlot(index, stack);
    }

    @Override
    public int getSlotLimit(int i) {
        return delegate.getSlotLimit(i);
    }

    @Override
    public boolean isItemValid(int i, @NotNull ItemStack itemStack) {
        return delegate.isItemValid(i, itemStack);
    }

    @Override
    public void onContentsChanged() {
        delegate.onContentsChanged();
    }

    @Override
    @ApiStatus.Internal
    @Nonnull
    public Object createSnapshot() {
        return delegate.createSnapshot();
    }

    @Override
    @ApiStatus.Internal
    public void restoreFromSnapshot(Object o) {
        delegate.restoreFromSnapshot(o);
    }
}
