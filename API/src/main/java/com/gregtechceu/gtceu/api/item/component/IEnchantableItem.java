package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public interface IEnchantableItem {

    boolean isEnchantable(ItemStack stack);

    int getEnchantmentValue(ItemStack stack);

    boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment);
}
