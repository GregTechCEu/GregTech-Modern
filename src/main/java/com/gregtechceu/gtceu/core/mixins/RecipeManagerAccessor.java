package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.google.common.collect.Multimap;
import org.jetbrains.annotations.VisibleForTesting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote RecipeManagerAccessor
 */
@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {

    @Accessor("byType")
    Multimap<RecipeType<?>, RecipeHolder<?>> getRawRecipes();

    @Accessor("byType")
    @VisibleForTesting
    void setRawRecipes(Multimap<RecipeType<?>, RecipeHolder<?>> recipes);
}
