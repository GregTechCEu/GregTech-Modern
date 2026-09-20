package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.RecipeIdAware;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeHolder.class)
public class RecipeHolderMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void gtceu$bindRecipeId(ResourceKey<Recipe<?>> id, Recipe<?> value, CallbackInfo ci) {
        if (value instanceof RecipeIdAware aware) {
            aware.setRecipeId(id.identifier());
        }
    }
}
