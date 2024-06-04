package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public interface IItemAttributes {

    ItemAttributeModifiers getAttributeModifiers(ItemStack stack);
}
