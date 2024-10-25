package com.gregtechceu.gtceu.api.transfer.item;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class ItemHandlerDelegate implements IItemHandlerModifiable {

    public IItemHandlerModifiable delegate;

    public ItemHandlerDelegate(IItemHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    protected void setDelegate(IItemHandlerModifiable delegate) {
        this.delegate = delegate;
    }

    //////////////////////////////////////
    // ****** OVERRIDE THESE ******//
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
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        delegate.setStackInSlot(slot, stack);
    }

    @Override
    @NotNull
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return delegate.insertItem(slot, stack, simulate);
    }

    @Override
    @NotNull
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return delegate.extractItem(slot, amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return delegate.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return delegate.isItemValid(slot, stack);
    }
}
