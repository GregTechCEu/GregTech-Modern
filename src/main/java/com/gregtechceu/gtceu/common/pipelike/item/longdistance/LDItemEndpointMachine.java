package com.gregtechceu.gtceu.common.pipelike.item.longdistance;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.LongDistanceEndpointMachine;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class LDItemEndpointMachine extends LongDistanceEndpointMachine {

    public LDItemEndpointMachine(IMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId, LDItemPipeType.INSTANCE);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static class ItemHandlerWrapper implements IItemTransfer {

        private final IItemTransfer delegate;

        public ItemHandlerWrapper(IItemTransfer delegate) {
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
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
            return delegate.insertItem(slot, stack, simulate, notifyChanges);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
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

        @NotNull
        @Override
        public Object createSnapshot() {
            return delegate.createSnapshot();
        }

        @Override
        public void restoreFromSnapshot(Object snapshot) {
            delegate.restoreFromSnapshot(snapshot);
        }
    }
}