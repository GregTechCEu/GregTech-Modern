package com.lowdragmc.lowdraglib.utils;

import net.minecraft.world.item.ItemStack;

public record ItemStackKey(ItemStack stack) {

    public static ItemStackKey of(ItemStack stack) {
        return new ItemStackKey(stack.copy());
    }

    public ItemStack[] getItemStack() {
        return new ItemStack[] { stack };
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ItemStackKey other && ItemStack.matches(stack, other.stack);
    }

    @Override
    public int hashCode() {
        return ItemStack.hashItemAndComponents(stack);
    }
}
