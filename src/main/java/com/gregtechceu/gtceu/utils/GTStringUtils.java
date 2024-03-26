package com.gregtechceu.gtceu.utils;

import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class GTStringUtils {


    /**
     * Better implementation of {@link ItemStack#toString()} which respects the stack-aware
     * {@link net.minecraft.world.item.Item#getDescriptionId(ItemStack)} method.
     *
     * @param stack the stack to convert
     * @return the string form of the stack
     */
    @Nonnull
    public static String itemStackToString(@Nonnull ItemStack stack) {
        return stack.getCount() + "x" + stack.getItem().getDescriptionId(stack);
    }
}
