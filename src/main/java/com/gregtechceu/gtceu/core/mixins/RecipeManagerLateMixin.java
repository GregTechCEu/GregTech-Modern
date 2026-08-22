package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.MapIngredientPool;
import com.gregtechceu.gtceu.api.recipe.lookup.RecipeManagerHandler;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.conditions.ICondition;

import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerLateMixin {

    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Shadow
    public abstract void replaceRecipes(Iterable<RecipeHolder<?>> recipes);

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$cloneVanillaRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        // use a linked hash map to keep the immutable map's order
        Map<ResourceLocation, RecipeHolder<?>> recipesByName = new LinkedHashMap<>(byName);
        // regenerate child recipes
        byName.values().forEach(holder -> {
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
                                recipesByName.put(id, new RecipeHolder<>(id, recipe));
                            }
                        });
            }
        });
        replaceRecipes(recipesByName.values());

        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (!(recipeType instanceof GTRecipeType gtRecipeType)) {
                continue;
            }
            gtRecipeType.beginStagingRecipes();
            gtRecipeType.getProxyRecipes().forEach((type, list) -> {
                Collection<RecipeHolder<?>> recipes = this.byType.get(type);
                if (recipes.isEmpty()) {
                    return;
                }
                RecipeManagerHandler.addProxyRecipesToLookup(recipes, gtRecipeType, type, list);
            });
            Collection<RecipeHolder<?>> recipesByID = this.byType.get(gtRecipeType);
            RecipeManagerHandler.addRecipesToLookup(recipesByID, gtRecipeType);
            gtRecipeType.getAdditionHandler().completeStaging();
        }
        MapIngredientPool.clear();
    }
}
