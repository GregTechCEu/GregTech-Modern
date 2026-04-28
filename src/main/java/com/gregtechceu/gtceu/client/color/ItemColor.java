package com.gregtechceu.gtceu.client.color;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface ItemColor {

    int getColor(ItemStack stack, int tintIndex);
}
