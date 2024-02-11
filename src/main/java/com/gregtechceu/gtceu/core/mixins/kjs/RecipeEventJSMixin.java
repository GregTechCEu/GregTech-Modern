package com.gregtechceu.gtceu.core.mixins.kjs;

import com.google.gson.JsonObject;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipeSerializer;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.core.mixins.RecipeManagerAccessor;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;
import com.gregtechceu.gtceu.integration.kjs.recipe.GTRecipeSchema;
import com.gregtechceu.gtceu.integration.kjs.recipe.components.GTRecipeComponents;
import com.llamalad7.mixinextras.sugar.Local;
import dev.latvian.mods.kubejs.recipe.RecipeJS;
import dev.latvian.mods.kubejs.recipe.RecipesEventJS;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author KilaBash
 * @date 2023/3/29
 * @implNote RecipeEventJSMixin
 */
@Mixin(RecipesEventJS.class)
public class RecipeEventJSMixin {
    @Shadow
    @Final
    public Collection<RecipeJS> addedRecipes;

    /**
     * Cuz KJS does a mixin {@link dev.latvian.mods.kubejs.core.mixin.common.RecipeManagerMixin} which breaks what we do {@link com.gregtechceu.gtceu.core.mixins.RecipeManagerMixin}.
     */
    @Inject(method = "post", at = @At(value = "RETURN"), remap = false)
    public void injectPost(RecipeManager recipeManager, Map<ResourceLocation, JsonObject> jsonMap, CallbackInfo ci, @Local(ordinal = 0) Map<ResourceLocation, Recipe<?>> recipesByName) {
        // (jankily) parse all GT recipes for extra ones to add, modify
        RecipesEventJS.runInParallel((() -> addedRecipes.forEach(recipe -> {
            if (recipe instanceof GTRecipeSchema.GTRecipeJS gtRecipe) {
                // get the recipe ID without the leading type path
                GTRecipeBuilder builder = ((GTRecipeType) Registry.RECIPE_TYPE.get(gtRecipe.type.id)).recipeBuilder(gtRecipe.idWithoutType());

                if (gtRecipe.getValue(GTRecipeSchema.DURATION) != null) {
                    builder.duration = gtRecipe.getValue(GTRecipeSchema.DURATION).intValue();
                }
                if (gtRecipe.getValue(GTRecipeSchema.DATA) != null) {
                    builder.data = gtRecipe.getValue(GTRecipeSchema.DATA);
                }
                if (gtRecipe.getValue(GTRecipeSchema.CONDITIONS) != null) {
                    builder.conditions.addAll(Arrays.stream(gtRecipe.getValue(GTRecipeSchema.CONDITIONS)).toList());
                }
                if (gtRecipe.getValue(GTRecipeSchema.IS_FUEL) != null) {
                    builder.isFuel = gtRecipe.getValue(GTRecipeSchema.IS_FUEL);
                }

                if (gtRecipe.getValue(GTRecipeSchema.ALL_INPUTS) != null) {
                    builder.input.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_INPUTS).entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer.fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey()).getFirst().write(gtRecipe, content)))
                            .toList()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }
                if (gtRecipe.getValue(GTRecipeSchema.ALL_OUTPUTS) != null) {
                    builder.output.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_OUTPUTS).entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer.fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey()).getSecond().write(gtRecipe, content)))
                            .toList()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }
                if (gtRecipe.getValue(GTRecipeSchema.ALL_TICK_INPUTS) != null) {
                    builder.tickInput.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_TICK_INPUTS).entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer.fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey()).getFirst().write(gtRecipe, content)))
                            .toList()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }
                if (gtRecipe.getValue(GTRecipeSchema.ALL_TICK_OUTPUTS) != null) {
                    builder.tickOutput.putAll(gtRecipe.getValue(GTRecipeSchema.ALL_TICK_OUTPUTS).entrySet().stream()
                        .map(entry -> Map.entry(entry.getKey(), Arrays.stream(entry.getValue())
                            .map(content -> entry.getKey().serializer.fromJsonContent(GTRecipeComponents.VALID_CAPS.get(entry.getKey()).getSecond().write(gtRecipe, content)))
                            .toList()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
                }

                builder.save(builtRecipe -> recipesByName.put(builtRecipe.getId(), GTRecipeSerializer.SERIALIZER.fromJson(builtRecipe.getId(), builtRecipe.serializeRecipe())));
            }
        })));

        // clone vanilla recipes for stuff like electric furnaces, etc
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                gtRecipeType.getLookup().removeAllRecipes();

                var proxyRecipes = gtRecipeType.getProxyRecipes();
                for (Map.Entry<RecipeType<?>, List<GTRecipe>> entry : proxyRecipes.entrySet()) {
                    var type = entry.getKey();
                    var recipes = entry.getValue();
                    recipes.clear();
                    for (var recipe : recipesByName.entrySet().stream().filter(recipe -> recipe.getValue().getType() == type).collect(Collectors.toSet())) {
                        recipes.add(gtRecipeType.toGTrecipe(recipe.getKey(), recipe.getValue()));
                    }
                }

                Stream.concat(
                        recipesByName.values().stream().filter(recipe -> recipe.getType() == gtRecipeType),
                        proxyRecipes.entrySet().stream().flatMap(entry -> entry.getValue().stream()))
                    .filter(GTRecipe.class::isInstance)
                    .map(GTRecipe.class::cast)
                    .forEach(gtRecipe -> gtRecipeType.getLookup().addRecipe(gtRecipe));
            }
        }
    }
}