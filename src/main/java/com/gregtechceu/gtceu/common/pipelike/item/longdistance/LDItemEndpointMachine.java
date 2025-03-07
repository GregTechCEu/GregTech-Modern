package com.gregtechceu.gtceu.common.pipelike.item.longdistance;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.storage.LongDistanceEndpointMachine;
import com.gregtechceu.gtceu.utils.GTTransferUtils;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LDItemEndpointMachine extends LongDistanceEndpointMachine {

    public LDItemEndpointMachine(IMachineBlockEntity metaTileEntityId) {
        super(metaTileEntityId, LDItemPipeType.INSTANCE);
    }

    @Override
    public @Nullable IItemHandlerModifiable getItemHandlerCap(@Nullable Direction side, boolean useCoverCapability) {
        if (isRemote() || getIoType() != IO.IN || side != getFrontFacing()) {
            return null;
        }
        var endpoint = getLink();
        if (endpoint == null) {
            return null;
        }
        return GTTransferUtils.getAdjacentItemHandler(getLevel(), endpoint.getPos(), endpoint.getOutputFacing())
                .map(ItemHandlerWrapper::new)
                .orElse(null);
    }

    public static class ItemHandlerWrapper implements IItemHandlerModifiable {

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

        @Override
        public void setStackInSlot(int i, @NotNull ItemStack itemStack) {
            if (delegate instanceof IItemHandlerModifiable modifiable) {
                modifiable.setStackInSlot(i, itemStack);
            }
        }
    }
}
