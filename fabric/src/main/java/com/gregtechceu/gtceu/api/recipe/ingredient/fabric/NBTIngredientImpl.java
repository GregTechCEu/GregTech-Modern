package com.gregtechceu.gtceu.api.recipe.ingredient.fabric;

import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote NBTIngredientImpl
 */
@SuppressWarnings("unused")
public class NBTIngredientImpl {
    public static Ingredient createNBTIngredient(ItemStack itemStack) {
        return DefaultCustomIngredients.nbt(Ingredient.of(itemStack), itemStack.getTag(), true);
    }
}
