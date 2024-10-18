package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import org.jetbrains.annotations.VisibleForTesting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/3/15
 * @implNote RecipeManagerAccessor
 */
@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {

    @Accessor("recipes")
    Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> getRawRecipes();

    @Accessor("recipes")
    @VisibleForTesting
    void setRawRecipes(Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes);

    @Invoker("byType")
    <C extends Container, T extends Recipe<C>> Map<ResourceLocation, Recipe<C>> getRecipeFromType(RecipeType<T> type);
}
