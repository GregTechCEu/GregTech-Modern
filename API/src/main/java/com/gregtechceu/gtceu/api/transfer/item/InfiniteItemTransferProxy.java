package com.gregtechceu.gtceu.api.transfer.item;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InfiniteItemTransferProxy extends ItemTransferDelegate {

    private final boolean infiniteSource;
    private final boolean infiniteSink;

    public InfiniteItemTransferProxy(IItemTransfer delegate, boolean infiniteSource, boolean infiniteSink) {
        super(delegate);

        this.infiniteSource = infiniteSource;
        this.infiniteSink = infiniteSink;
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        if (infiniteSink)
            return ItemStack.EMPTY;

        return super.insertItem(slot, stack, simulate, notifyChanges);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        if (infiniteSource)
            return delegate.getStackInSlot(slot).copyWithCount(amount);

        return super.extractItem(slot, amount, simulate, notifyChanges);
    }
}
