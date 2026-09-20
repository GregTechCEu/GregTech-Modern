package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.StrictShapedRecipe;

import net.minecraft.recipebook.PlaceRecipeHelper;
import net.minecraft.world.item.crafting.Recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlaceRecipeHelper.class)
public interface PlaceRecipeHelperMixin {
    @Inject(method = "placeRecipe(IILnet/minecraft/world/item/crafting/Recipe;Ljava/lang/Iterable;Lnet/minecraft/recipebook/PlaceRecipeHelper$Output;)V",
            at = @At("HEAD"), cancellable = true)
    private static <T> void gtceu$placeStrictRecipe(int width, int height, Recipe<?> recipe, Iterable<T> entries,
                                                  PlaceRecipeHelper.Output<T> output, CallbackInfo ci) {
        if (recipe instanceof StrictShapedRecipe strict) {
            PlaceRecipeHelper.placeRecipe(width, height, strict.getWidth(), strict.getHeight(), entries, output);
            ci.cancel();
        }
    }
}
