package com.gregtechceu.gtceu.api.transfer.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

public class EntitySlotInvWrapper implements IItemHandlerModifiable {

    private final Entity entity;

    public EntitySlotInvWrapper(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setStackInSlot(int slot, @NotNull ItemStack stack) {
        entity.getSlot(slot).set(stack);
    }

    @Override
    public int getSlots() {
        return 0;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return entity.getSlot(slot).get();
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        ItemStack existing = entity.getSlot(slot).get();
        if (ItemStack.isSameItemSameTags(stack, existing)) {
            int maxInserted = existing.getMaxStackSize() - existing.getCount();
            int inserted = Math.min(maxInserted, stack.getCount());
            if (!simulate) {
                boolean success = entity.getSlot(slot).set(existing.copyWithCount(existing.getCount() + inserted));
                if (!success) return stack;
            }
            return stack.copyWithCount(stack.getCount() - inserted);
        }
        return stack;
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = entity.getSlot(slot).get();
        if (!simulate)
            setStackInSlot(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public int getSlotLimit(int slot) {
        return entity.getSlot(slot).get().getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return entity.getSlot(slot) != SlotAccess.NULL;
    }
}
