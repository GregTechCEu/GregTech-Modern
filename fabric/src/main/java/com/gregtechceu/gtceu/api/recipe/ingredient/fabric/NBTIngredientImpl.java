package com.gregtechceu.gtceu.api.recipe.ingredient.fabric;

import net.fabricmc.fabric.impl.recipe.ingredient.builtin.NbtIngredient;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote NBTIngredientImpl
 */
public class NBTIngredientImpl {
    public static Ingredient createNBTIngredient(ItemStack itemStack) {
        return new NbtIngredient(Ingredient.of(itemStack), itemStack.getTag(), true).toVanilla();
    }
}
