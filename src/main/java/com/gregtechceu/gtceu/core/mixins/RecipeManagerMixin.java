package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.gregtechceu.gtceu.common.item.armor.PowerlessJetpack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

/**
 * Only fires if KubeJS is not interacting with GT recipes.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin {

    @Shadow
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$cloneVanillaRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        PowerlessJetpack.FUELS.clear();
        for (RecipeType<?> recipeType : ForgeRegistries.RECIPE_TYPES) {
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                continue;
            }
            gtRecipeType.getLookup().removeAllRecipes();
            gtRecipeType.getProxyRecipes().forEach((type, list) -> {
                var recipesByID = recipes.get(type);
                if (recipesByID == null) {
                    return;
                }
                RecipeManagerHandler.addProxyRecipesToLookup(recipesByID, gtRecipeType, type, list);
            });
            var recipesByID = recipes.get(gtRecipeType);
            if (recipesByID == null) {
                continue;
            }
            RecipeManagerHandler.addRecipesToLookup(recipesByID, gtRecipeType);
        }
    }
}
