package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;
import com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;
import com.gregtechceu.gtceu.core.IGTRecipeManager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

/**
 * Only fires if KubeJS is not interacting with GT recipes.
 */
@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin implements IGTRecipeManager {

    @Shadow
    private Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>> recipes;

    @Unique
    private WeakReference<Map<RecipeType<?>, Map<ResourceLocation, Recipe<?>>>> gtceu$jetpackFuelsSource;
    @Unique
    private Object2IntMap<FluidIngredient> gtceu$jetpackFuels;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$cloneVanillaRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        for (RecipeType<?> recipeType : ForgeRegistries.RECIPE_TYPES) {
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                continue;
            }
            gtRecipeType.beginStagingRecipes();
            gtRecipeType.getProxyRecipes().forEach((type, list) -> {
                var recipesByID = recipes.get(type);
                if (recipesByID == null) {
                    return;
                }
                RecipeManagerHandler.addProxyRecipesToLookup(recipesByID, gtRecipeType, type, list);
            });
            var recipesByID = recipes.get(gtRecipeType);
            if (recipesByID == null) {
                gtRecipeType.getAdditionHandler().completeStaging();
                continue;
            }
            RecipeManagerHandler.addRecipesToLookup(recipesByID, gtRecipeType);
            gtRecipeType.getAdditionHandler().completeStaging();
        }
        MapIngredientPool.clear();
    }

    @Override
    public Object2IntMap<FluidIngredient> gtceu$getJetpackFuels() {
        if (gtceu$jetpackFuels == null || gtceu$jetpackFuelsSource == null ||
                gtceu$jetpackFuelsSource.get() != recipes) {
            gtceu$jetpackFuelsSource = new WeakReference<>(recipes);
            gtceu$jetpackFuels = gtceu$buildJetpackFuels();
        }
        return gtceu$jetpackFuels;
    }

    @Unique
    private Object2IntMap<FluidIngredient> gtceu$buildJetpackFuels() {
        Map<ResourceLocation, Recipe<?>> fuelRecipes = recipes.get(GTRecipeTypes.COMBUSTION_GENERATOR_FUELS);
        if (fuelRecipes == null || fuelRecipes.isEmpty()) {
            return Object2IntMaps.emptyMap();
        }
        Object2IntMap<FluidIngredient> fuels = new Object2IntOpenHashMap<>();
        for (Recipe<?> recipe : fuelRecipes.values()) {
            if (!(recipe instanceof GTRecipe gtRecipe)) {
                continue;
            }
            List<Content> contents = gtRecipe.getInputContents(FluidRecipeCapability.CAP);
            if (contents.isEmpty()) {
                continue;
            }
            FluidIngredient fluid = FluidRecipeCapability.CAP.of(contents.get(0).content());
            long burnTime = gtRecipe.duration * gtRecipe.getOutputEUt().getTotalEU() / GTValues.V[GTValues.LV];
            fuels.putIfAbsent(fluid, (int) Math.max(1, burnTime));
        }
        return fuels;
    }
}
