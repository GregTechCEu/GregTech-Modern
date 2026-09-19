package com.gregtechceu.gtceu.utils;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

public class EntitySlotsInvWrapper implements IItemHandlerModifiable {

    private final Entity entity;

    public EntitySlotsInvWrapper(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        entity.getSlot(slot).set(stack);
    }

    @Override
    public int getSlots() {
        return 0; // there is no way to get the amount of slots
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return entity.getSlot(slot).get();
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (entity.getSlot(slot).get().isEmpty()) {
            if (!simulate)
                entity.getSlot(slot).set(stack);
            return ItemStack.EMPTY;
        }
        if (isItemValid(slot, stack)) {
            ItemStack current = entity.getSlot(slot).get();
            int count = Math.min(current.getMaxStackSize() - current.getCount(), stack.getCount());
            if (!simulate)
                entity.getSlot(slot).get().grow(count);
            ItemStack result = current.copy();
            result.shrink(count);
            return result;
        }
        return stack;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        ItemStack stack = entity.getSlot(slot).get();
        if (simulate || entity.getSlot(slot).set(ItemStack.EMPTY))
            return stack;
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 0;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (entity.getSlot(slot).get().isEmpty())
            return true;
        return ItemStack.isSameItemSameComponents(stack, entity.getSlot(slot).get());
    }
}
