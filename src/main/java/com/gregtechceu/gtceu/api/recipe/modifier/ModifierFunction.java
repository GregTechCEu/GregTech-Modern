package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeCondition;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@FunctionalInterface
public interface ModifierFunction {

    ModifierFunction IDENTITY = r -> r;

    @Nullable
    GTRecipe apply(@NotNull GTRecipe recipe);

    default ModifierFunction compose(@NotNull ModifierFunction before) {
        return r -> {
            var between = before.apply(r);
            if (between == null) return null;
            return this.apply(between);
        };
    }

    default ModifierFunction andThen(@NotNull ModifierFunction after) {
        return r -> {
            var between = this.apply(r);
            if (between == null) return null;
            return after.apply(between);
        };
    }

    static FunctionBuilder builder() {
        return new FunctionBuilder();
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    final class FunctionBuilder {

        private int multiplyParallels = 1;
        private int addOCs = 0;
        private ContentModifier eutModifier = ContentModifier.IDENTITY;
        private ContentModifier durationModifier = ContentModifier.IDENTITY;
        private ContentModifier inputModifier = ContentModifier.IDENTITY;
        private ContentModifier outputModifier = ContentModifier.IDENTITY;
        private ContentModifier tickInputModifier = ContentModifier.IDENTITY;
        private ContentModifier tickOutputModifier = ContentModifier.IDENTITY;
        private final List<RecipeCondition> addedConditions = new ArrayList<>();

        public FunctionBuilder() {}

        public FunctionBuilder conditions(RecipeCondition... conditions) {
            addedConditions.addAll(Arrays.asList(conditions));
            return this;
        }

        public FunctionBuilder modifyAllContents(ContentModifier cm) {
            inputModifier = cm;
            outputModifier = cm;
            tickInputModifier = cm;
            tickOutputModifier = cm;
            return this;
        }

        public ModifierFunction build() {
            return recipe -> {
                var newConditions = new ArrayList<>(recipe.conditions);
                newConditions.addAll(addedConditions);
                var preEUt = RecipeHelper.getRealEUt(recipe);
                var copied = new GTRecipe(recipe.recipeType, recipe.id,
                        inputModifier.applyContents(recipe.inputs),
                        outputModifier.applyContents(recipe.outputs),
                        tickInputModifier.applyContents(recipe.tickInputs),
                        tickOutputModifier.applyContents(recipe.tickOutputs),
                        new HashMap<>(recipe.inputChanceLogics), new HashMap<>(recipe.outputChanceLogics),
                        new HashMap<>(recipe.tickInputChanceLogics), new HashMap<>(recipe.tickOutputChanceLogics),
                        newConditions, new ArrayList<>(recipe.ingredientActions),
                        recipe.data, recipe.duration, recipe.isFuel, recipe.recipeCategory);
                copied.parallels *= multiplyParallels;
                copied.ocLevel += addOCs;
                if (recipe.data.getBoolean("duration_is_total_cwu")) {
                    copied.duration = (int) Math.max(1, (copied.duration * (1f - 0.025f * addOCs)));
                } else {
                    copied.duration = Math.max(1, durationModifier.apply(recipe.duration));
                }
                if (eutModifier != ContentModifier.IDENTITY) {
                    long eut = Math.max(1, eutModifier.apply(Math.abs(preEUt)));
                    if (preEUt > 0)
                        copied.tickInputs.put(EURecipeCapability.CAP, EURecipeCapability.makeEUContent(eut));
                    else copied.tickOutputs.put(EURecipeCapability.CAP, EURecipeCapability.makeEUContent(eut));
                }
                return copied;
            };
        }
    }
}
