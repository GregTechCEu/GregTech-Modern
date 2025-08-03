package com.gregtechceu.gtceu.gametest.util;

import net.minecraft.world.item.ItemStack;

public class TestUtils {

    // Compares two itemstacks' items and amounts
    // DOES NOT CHECK TAGS OR NBT ETC!
    public static boolean isItemStackEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItem(stack1, stack2) && stack1.getCount() == stack2.getCount();
    }
}
