package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.world.item.ItemStack;

@FunctionalInterface
public interface IRecipeRemainder extends IItemComponent {

    ItemStack getRecipeRemained(ItemStack itemStack);
}
