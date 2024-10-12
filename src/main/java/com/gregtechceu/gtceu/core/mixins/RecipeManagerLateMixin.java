package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
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

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Mixin(value = RecipeManager.class, priority = 1500)
public abstract class RecipeManagerLateMixin {

    @Shadow
    private Multimap<RecipeType<?>, RecipeHolder<?>> byType;

    @Shadow
    private Map<ResourceLocation, RecipeHolder<?>> byName;

    @Inject(method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At(value = "TAIL"))
    private void gtceu$cloneVanillaRecipes(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager,
                                           ProfilerFiller profiler, CallbackInfo ci) {
        var recipesByName = new HashMap<>(byName);
        byName.values().forEach(holder -> {
            if (holder.value() instanceof GTRecipe gtRecipe) {
                new GTRecipeBuilder(gtRecipe, gtRecipe.recipeType)
                        .id(holder.id().withPath(path -> path.substring(path.indexOf('/') + 1)))
                        .onSave(gtRecipe.recipeType.getRecipeBuilder().onSave)
                        .save(new RecipeOutput() {

                            @Override
                            public Advancement.Builder advancement() {
                                return Advancement.Builder.recipeAdvancement()
                                        .parent(RecipeBuilder.ROOT_RECIPE_ADVANCEMENT);
                            }

                            @Override
                            public void accept(@NotNull ResourceLocation id, @NotNull Recipe<?> recipe,
                                               @Nullable AdvancementHolder advancement, ICondition... conditions) {
                                recipesByName.put(id, new RecipeHolder<>(id, recipe));
                            }
                        });
            }
        });
        gtceu$replaceRecipes(recipesByName);

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
                    Stream.concat(
                            this.byType.get(gtRecipeType).stream(),
                            proxyRecipes.entrySet().stream().flatMap(entry -> entry.getValue().stream()))
                            .filter(holder -> holder != null && holder.value() instanceof GTRecipe)
                            .forEach(holder -> {
                                GTRecipe recipe = (GTRecipe) holder.value();
                                recipe.setId(holder.id());
                                gtRecipeType.getLookup().addRecipe(recipe);
                            });
                } else if (!proxyRecipes.isEmpty()) {
                    proxyRecipes.values().stream()
                            .flatMap(List::stream)
                            .forEach(gtRecipe -> gtRecipeType.getLookup().addRecipe(gtRecipe.value()));
                }
            }
        }
    }

    @Unique
    public void gtceu$replaceRecipes(Map<ResourceLocation, RecipeHolder<?>> map) {
        byName = map;

        var recipesByType = ImmutableMultimap.<RecipeType<?>, RecipeHolder<?>>builder();

        for (var entry : map.entrySet()) {
            recipesByType.put(entry.getValue().value().getType(), entry.getValue());
        }

        byType = recipesByType.build();
    }
}
