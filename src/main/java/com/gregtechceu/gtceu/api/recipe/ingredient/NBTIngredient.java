package com.gregtechceu.gtceu.api.recipe.ingredient;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * @author KilaBash
 * @date 2023/2/21
 * @implNote NBTIngredient
 */
public class NBTIngredient {
    @ExpectPlatform
    public static Ingredient createNBTIngredient(ItemStack itemStack) {
        throw new AssertionError();
    }
}
