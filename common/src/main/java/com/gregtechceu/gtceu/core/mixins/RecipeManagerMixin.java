package com.gregtechceu.gtceu.core.mixins;

import com.google.gson.JsonElement;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 1100 /*1075*/) // bump priority up to make this run after AU but before KJS
public abstract class RecipeManagerMixin {

    @Shadow private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "HEAD"))
    private void injectApplyHead(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        GTCEu.LOGGER.info("Ignore errors about 'duplicated recipes', they're simply overrides. (DO note them if your custom recipe isn't working.)");
        long startTime = System.currentTimeMillis();
        GTRecipes.recipeRemoval(rl -> {
            if (map.remove(rl) == null) {
                GTCEu.LOGGER.error("failed to remove recipe {}, could not find matching recipe", rl);
            }
        });
        GTRecipes.recipeAddition(recipe -> {
            var id = recipe.getId();
            if (map.put(id, recipe.serializeRecipe()) != null) {
                GTCEu.LOGGER.error("duplicated recipe: {}", id);
            }
        });
        GTCEu.LOGGER.info("GregTech Recipe loading took {}ms", System.currentTimeMillis() - startTime);
    }

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void injectApplyTail(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                var proxyRecipes = gtRecipeType.getProxyRecipes();
                for (Map.Entry<RecipeType<?>, List<GTRecipe>> entry : proxyRecipes.entrySet()) {
                    var type = entry.getKey();
                    var recipes = entry.getValue();
                    recipes.clear();
                    for (var recipe : this.recipes.get(type).entrySet()) {
                        recipes.add(gtRecipeType.toGTrecipe(recipe.getKey(), recipe.getValue()));
                    }
                }
            }
        }
    }
}
