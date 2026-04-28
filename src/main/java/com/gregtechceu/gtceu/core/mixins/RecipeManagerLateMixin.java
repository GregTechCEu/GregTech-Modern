package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.recipe.lookup.StagingRecipeDB;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.common.conditions.ICondition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerLateMixin {

    @Shadow
    private RecipeMap recipes;

    @Inject(method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$cloneVanillaRecipes(RecipeMap map, ResourceManager resourceManager,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        var recipesByName = new HashMap<ResourceKey<Recipe<?>>, RecipeHolder<?>>();
        this.recipes.values().forEach(holder -> recipesByName.put(holder.id(), holder));
        this.recipes.values().forEach(holder -> {
            if (holder.value() instanceof GTRecipe gtRecipe) {
                new GTRecipeBuilder(gtRecipe, gtRecipe.recipeType)
                        .id(holder.id().identifier().withPath(path -> path.substring(path.indexOf('/') + 1)))
                        .onSave(gtRecipe.recipeType.getRecipeBuilder().onSave)
                        .save(new RecipeOutput() {

                            @SuppressWarnings("removal")
                            @Override
                            public Advancement.@NotNull Builder advancement() {
                                return Advancement.Builder.recipeAdvancement()
                                        .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                            }

                            @Override
                            public void accept(@NotNull ResourceKey<Recipe<?>> id, @NotNull Recipe<?> recipe,
                                               @Nullable AdvancementHolder advancement,
                                               ICondition @NotNull... conditions) {
                                recipesByName.put(id, new RecipeHolder<>(id, recipe));
                            }

                            @Override
                            public void includeRootAdvancement() {}
                        });
            }
        });
        gtceu$replaceRecipes(recipesByName.values());

        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                var stagingDB = new StagingRecipeDB();

                var proxyRecipes = gtRecipeType.getProxyRecipes();
                for (Map.Entry<RecipeType<?>, List<RecipeHolder<GTRecipe>>> entry : proxyRecipes.entrySet()) {
                    var type = entry.getKey();
                    var recipes = entry.getValue();
                    recipes.clear();
                    for (var recipe : gtceu$byType(this.recipes, type)) {
                        recipes.add(gtRecipeType.toGTRecipe(recipe));
                    }
                }

                Collection<RecipeHolder<GTRecipe>> typedRecipes = gtceu$typedByType(this.recipes, gtRecipeType);
                if (!typedRecipes.isEmpty()) {
                    Stream.concat(
                            typedRecipes.stream(),
                            proxyRecipes.entrySet().stream().flatMap(entry -> entry.getValue().stream()))
                            .filter(holder -> holder != null && holder.value() instanceof GTRecipe)
                            .forEach(holder -> {
                                GTRecipe recipe = (GTRecipe) holder.value();
                                recipe.setId(holder.id().identifier());
                                stagingDB.add(recipe);
                            });
                } else if (!proxyRecipes.isEmpty()) {
                    proxyRecipes.values().stream()
                            .flatMap(List::stream)
                            .forEach(gtRecipe -> stagingDB.add(gtRecipe.value()));
                }

                stagingDB.populateDB(gtRecipeType.db());
            }
        }
    }

    @Unique
    public void gtceu$replaceRecipes(Iterable<RecipeHolder<?>> recipes) {
        this.recipes = RecipeMap.create(recipes);
    }

    @Unique
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Collection<RecipeHolder<?>> gtceu$byType(RecipeMap recipes, RecipeType<?> type) {
        return (Collection) recipes.byType((RecipeType) type);
    }

    @Unique
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Collection<RecipeHolder<GTRecipe>> gtceu$typedByType(RecipeMap recipes, GTRecipeType type) {
        return (Collection) recipes.byType((RecipeType) type);
    }
}
