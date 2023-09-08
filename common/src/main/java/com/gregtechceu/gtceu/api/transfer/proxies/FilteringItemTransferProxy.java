package com.gregtechceu.gtceu.api.transfer.proxies;

import com.gregtechceu.gtceu.api.cover.filter.ItemFilter;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import lombok.Getter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilteringItemTransferProxy implements IItemTransfer {
    private final IItemTransfer delegate;

    @Getter
    private ItemFilter filter;

    public FilteringItemTransferProxy(IItemTransfer delegate, ItemFilter filter) {
        this.delegate = delegate;
        this.filter = filter;
    }

    public void setFilter(@Nullable ItemFilter filter) {
        this.filter = filter != null ? filter : ItemFilter.EMPTY;
    }

    @Override
    @Nonnull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        if (!this.filter.test(stack))
            return stack.copy();

        return delegate.insertItem(slot, stack, simulate, notifyChanges);
    }

    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!this.filter.test(stack))
            return stack.copy();

        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    @Nonnull
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        var extracted = delegate.extractItem(slot, amount, true, notifyChanges);

        if (!this.filter.test(extracted))
            return ItemStack.EMPTY;

        if (!simulate) {
            extracted = delegate.extractItem(slot, amount, false, notifyChanges);
        }

        return extracted;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        var extracted = delegate.extractItem(slot, amount, true);

        if (!this.filter.test(extracted))
            return ItemStack.EMPTY;

        if (!simulate) {
            extracted = delegate.extractItem(slot, amount, false);
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
    public ItemStack getStackInSlot(int slot) {
        return delegate.getStackInSlot(slot);
    }

    @Override
    public void setStackInSlot(int index, ItemStack stack) {
        delegate.setStackInSlot(index, stack);
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
    public void restoreFromSnapshot(Object snapshot) {
        delegate.restoreFromSnapshot(snapshot);
    }
}
