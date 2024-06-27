package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.data.recipe.GTRecipes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(value = RecipeManager.class, priority = 500)
public abstract class RecipeManagerMixin {

    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("HEAD"))
    private void gtceu$removeRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager pResourceManager,
                                     ProfilerFiller pProfiler, CallbackInfo ci) {
        GTRecipes.recipeRemoval(map::remove);
    }

    // only fires if KJS is NOT loaded.
    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$cloneVanillaRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                gtRecipeType.getLookup().removeAllRecipes();

                var proxyRecipes = gtRecipeType.getProxyRecipes();
                for (Map.Entry<RecipeType<?>, List<RecipeHolder<GTRecipe>>> entry : proxyRecipes.entrySet()) {
                    var type = entry.getKey();
                    var recipes = entry.getValue();
                    recipes.clear();
                    if (this.byType.containsKey(type)) {
                        for (var recipe : this.byType.get(type)) {
                            recipes.add(gtRecipeType.toGTRecipe(recipe));
                        }
                    }
                }

                if (this.byType.containsKey(gtRecipeType)) {
                    // noinspection unchecked
                    Stream.concat(
                            this.byType.get(gtRecipeType).stream(),
                            proxyRecipes.entrySet().stream().flatMap(entry -> entry.getValue().stream()))
                            .filter(holder -> holder != null && holder.value() instanceof GTRecipe)
                            .forEach(gtRecipeHolder -> gtRecipeType.getLookup()
                                    .addRecipe((RecipeHolder<GTRecipe>) gtRecipeHolder));
                } else if (!proxyRecipes.isEmpty()) {
                    proxyRecipes.values().stream()
                            .flatMap(List::stream)
                            .forEach(gtRecipe -> gtRecipeType.getLookup().addRecipe(gtRecipe));
                }
            }
        }
    }
}
