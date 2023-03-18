package com.gregtechceu.gtceu.api.recipe.ingredient.forge;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote NBTIngredientImpl
 */
public class NBTIngredientImpl {
    public static Ingredient createNBTIngredient(ItemStack itemStack) {
        return StrictNBTIngredient.of(itemStack);
    }
}
