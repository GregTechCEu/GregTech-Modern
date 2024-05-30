package com.gregtechceu.gtceu.api.recipe.ingredient;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.StrictNBTIngredient;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote NBTIngredient
 */
public class NBTIngredient {

    public static Ingredient createNBTIngredient(ItemStack itemStack) {
        return StrictNBTIngredient.of(itemStack);
    }
}
