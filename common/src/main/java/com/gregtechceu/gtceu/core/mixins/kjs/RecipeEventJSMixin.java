package com.gregtechceu.gtceu.core.mixins.kjs;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerAccessor;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

/**
 * @author KilaBash
 * @date 2023/3/29
 * @implNote RecipeEventJSMixin
 */
@Mixin(RecipesEventJS.class)
public class RecipeEventJSMixin {
    /**
     * Cuz KJS does a mixin {@link dev.latvian.mods.kubejs.core.mixin.common.RecipeManagerMixin} which breaks what we do {@link com.gregtechceu.gtceu.core.mixins.RecipeManagerMixin}.
     */
    @Inject(method = "post", at = @At(value = "RETURN"), remap = false)
    public void injectPost(RecipeManager recipeManager, Map<ResourceLocation, JsonObject> jsonMap, CallbackInfo ci) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                var proxyRecipes = gtRecipeType.getProxyRecipes();
                for (Map.Entry<RecipeType<?>, List<GTRecipe>> entry : proxyRecipes.entrySet()) {
                    var type = entry.getKey();
                    var recipes = entry.getValue();
                    recipes.clear();
                    for (var recipe : ((RecipeManagerAccessor)recipeManager).getRawRecipes().get(type).entrySet()) {
                        recipes.add(gtRecipeType.toGTrecipe(recipe.getKey(), recipe.getValue()));
                    }
                }
            }
        }
    }
}
