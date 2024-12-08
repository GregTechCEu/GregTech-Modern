package com.gregtechceu.gtceu.common.pipelike.item.longdistance;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.LongDistanceEndpointMachine;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

public class LDItemEndpointMachine extends LongDistanceEndpointMachine {

    public LDItemEndpointMachine(IMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId, LDItemPipeType.INSTANCE);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class ItemHandlerWrapper implements IItemHandler {

        private final IItemHandler delegate;

        public ItemHandlerWrapper(IItemHandler delegate) {
            this.delegate = delegate;
        }

        @Override
        public int getSlots() {
            return delegate.getSlots();
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return delegate.getStackInSlot(slot);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            return delegate.insertItem(slot, stack, simulate);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
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
}
