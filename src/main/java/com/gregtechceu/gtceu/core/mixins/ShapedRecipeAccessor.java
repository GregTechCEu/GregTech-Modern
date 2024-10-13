package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.ShapedRecipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author KilaBash
 * @date 2023/7/24
 * @implNote ShapedRecipeAccessor
 */
@Mixin(ShapedRecipe.class)
public interface ShapedRecipeAccessor {

    @Accessor
    ItemStack getResult();

    @Accessor
    String getGroup();

    @Accessor
    CraftingBookCategory getCategory();

    @Accessor
    boolean getShowNotification();
}
