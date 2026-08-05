package com.gregtechceu.gtceu.data.dynamic;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.trait.customlogic.SteamBoilerLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.gregtechceu.gtceu.common.data.GTRecipes;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.pack.GTDynamicDataPack;
import com.gregtechceu.gtceu.data.recipe.GTCraftingComponents;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.*;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.WithConditions;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApiStatus.Internal
public final class DynamicRecipeHandler {

    private DynamicRecipeHandler() {}

    // reload time spent in handleRecipesEarly, in milliseconds.
    private static final AtomicLong earlyLoadElapsed = new AtomicLong();

    public static void handleRecipesEarly(Map<ResourceLocation, JsonElement> map, HolderLookup.Provider registries,
                                          final ConditionalOps<JsonElement> serializationContext) {
        long startTime = System.currentTimeMillis();

        // first, remove old recipes & clear caches
        GTRecipes.recipeRemoval(map::remove);
        SteamBoilerLogic.clearBoilerRecipeCaches();
        GTCraftingComponents.init();

        GTRecipes.recipeAddition(new RecipeOutput() {

            @Override
            public Advancement.@NotNull Builder advancement() {
                // noinspection removal
                return Advancement.Builder.recipeAdvancement().parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
            }

            @Override
            public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                               @Nullable AdvancementHolder advancement, ICondition @NotNull... conditions) {
                JsonElement recipeJson = Recipe.CONDITIONAL_CODEC
                        .encodeStart(serializationContext, Optional.of(new WithConditions<>(recipe, conditions)))
                        .getOrThrow();
                map.put(id, recipeJson);

                if (ConfigHolder.INSTANCE.dev.dumpRecipes) {
                    // add the recipe JSON to the generated datapack if data dumping is enabled so it can be dumped
                    // immediately or with a command
                    GTDynamicDataPack.addResource(GTDynamicDataPack.RECIPE_ID_CONVERTER.idToFile(id), recipeJson);
                }

                if (advancement != null) {
                    GTDynamicDataPack.addAdvancement(advancement, serializationContext);
                }
            }
        });

        earlyLoadElapsed.set(System.currentTimeMillis() - startTime);
    }

    public static void handleRecipesLate(RecipeManager recipeManager) {
        long startTime = System.currentTimeMillis();

        cloneVanillaRecipes(recipeManager);
        addRecipesToLookup(recipeManager);

        long elapsed = (System.currentTimeMillis() - startTime) + earlyLoadElapsed.get();
        GTCEu.LOGGER.info("GregTech dynamic recipe generation took {}ms", elapsed);
    }

    private static void addRecipesToLookup(RecipeManager recipeManager) {
        for (RecipeType<?> t : BuiltInRegistries.RECIPE_TYPE) {
            if (!(t instanceof GTRecipeType recipeType)) {
                continue;
            }
            recipeType.beginStagingRecipes();

            for (var entry : recipeType.getProxyRecipes().entrySet()) {
                RecipeType<?> proxyRecipeType = entry.getKey();
                Collection<? extends RecipeHolder<?>> recipes = recipeManager.getAllRecipesFor(proxyRecipeType);
                if (recipes.isEmpty()) {
                    continue;
                }
                List<RecipeHolder<GTRecipe>> proxyRecipes = entry.getValue();
                RecipeManagerHandler.addProxyRecipesToLookup(recipes, recipeType, proxyRecipeType, proxyRecipes);
            }

            Collection<? extends RecipeHolder<?>> recipesByID = recipeManager.getAllRecipesFor(recipeType);
            RecipeManagerHandler.addRecipesToLookup(recipesByID, recipeType);
            recipeType.completeStagingRecipes();
        }
        MapIngredientPool.clear();
    }

    private static void cloneVanillaRecipes(RecipeManager recipeManager) {
        Collection<RecipeHolder<?>> originalRecipes = recipeManager.getRecipes();

        // use a linked map to keep the immutable map's order
        // this is a map so duplicate recipes can replace older ones easily without adding a bunch of useless entries to
        // a list or set
        Map<ResourceLocation, RecipeHolder<?>> replacementRecipes = originalRecipes.stream()
                .collect(Collectors.toMap(RecipeHolder::id, Function.identity(),
                        (oldValue, value) -> value, LinkedHashMap::new));

        // regenerate child recipes
        originalRecipes.forEach(holder -> {
            if (holder.value() instanceof GTRecipe gtRecipe) {
                new GTRecipeBuilder(gtRecipe, gtRecipe.recipeType)
                        .id(holder.id().withPath(path -> path.substring(path.indexOf('/') + 1)))
                        .onSave(gtRecipe.recipeType.getRecipeBuilder().onSave)
                        .save(new RecipeOutput() {

                            @SuppressWarnings("removal")
                            @Override
                            public Advancement.@NotNull Builder advancement() {
                                return Advancement.Builder.recipeAdvancement()
                                        .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                            }

                            @Override
                            public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                                               @Nullable AdvancementHolder advancement,
                                               ICondition @NotNull... conditions) {
                                replacementRecipes.put(id, new RecipeHolder<>(id, recipe));
                            }
                        });
            }
        });
        recipeManager.replaceRecipes(replacementRecipes.values());
    }
}
