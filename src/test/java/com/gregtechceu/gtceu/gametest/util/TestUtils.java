package com.gregtechceu.gtceu.gametest.util;

import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;

import net.minecraft.world.item.ItemStack;

public class TestUtils {

    // Compares two itemstacks' items and amounts
    // DOES NOT CHECK TAGS OR NBT ETC!
    public static boolean isItemStackEqual(ItemStack stack1, ItemStack stack2) {
        return ItemStack.isSameItem(stack1, stack2) && stack1.getCount() == stack2.getCount();
    }

    // Forces a structure check on multiblocks after being placed, to avoid having to wait ticks.
    // Ideally this doesn't need to happen, but it seems not doing this makes the multiblock tests flakey
    public static void formMultiblock(MultiblockControllerMachine controller) {
        controller.getPattern().checkPatternAt(controller.getMultiblockState(), false);
        controller.onStructureFormed();
    }
}
