package com.gregtechceu.gtceu.utils;

import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.NotNull;

public class GTStringUtils {


    /**
     * Better implementation of {@link ItemStack#toString()} which respects the stack-aware
     * {@link net.minecraft.world.item.Item#getDescriptionId(ItemStack)} method.
     *
     * @param stack the stack to convert
     * @return the string form of the stack
     */
    @NotNull
    public static String itemStackToString(@NotNull ItemStack stack) {
        return stack.getCount() + "x" + stack.getItem().getDescriptionId(stack);
    }
}
