package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
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

    ModifierFunction NULL = r -> null;
    ModifierFunction IDENTITY = r -> r;

    static ModifierFunction nullWithLog(Class<?> type, MetaMachine actual) {
        GTCEu.LOGGER.error("Incorrect use of modifier, expected machine of type {}, received {}", type.getSimpleName(),
                actual.getDefinition().getName());
        return NULL;
    }

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

        private int parallels = 1;
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

        public FunctionBuilder eutMultiplier(double multiplier) {
            eutModifier = ContentModifier.multiplier(multiplier);
            return this;
        }

        public FunctionBuilder durationMultiplier(double multiplier) {
            durationModifier = ContentModifier.multiplier(multiplier);
            return this;
        }

        public ModifierFunction build() {
            if (parallels == 0) return NULL;
            return recipe -> {
                var newConditions = new ArrayList<>(recipe.conditions);
                newConditions.addAll(addedConditions);
                var copied = new GTRecipe(recipe.recipeType, recipe.id,
                        inputModifier.applyContents(recipe.inputs),
                        outputModifier.applyContents(recipe.outputs),
                        tickInputModifier.applyAllButEU(recipe.tickInputs),
                        tickOutputModifier.applyAllButEU(recipe.tickOutputs),
                        new HashMap<>(recipe.inputChanceLogics), new HashMap<>(recipe.outputChanceLogics),
                        new HashMap<>(recipe.tickInputChanceLogics), new HashMap<>(recipe.tickOutputChanceLogics),
                        newConditions, new ArrayList<>(recipe.ingredientActions),
                        recipe.data, recipe.duration, recipe.isFuel, recipe.recipeCategory);
                copied.parallels *= parallels;
                copied.ocLevel += addOCs;
                if (recipe.data.getBoolean("duration_is_total_cwu")) {
                    copied.duration = (int) Math.max(1, (copied.duration * (1f - 0.025f * addOCs)));
                } else {
                    copied.duration = Math.max(1, durationModifier.apply(recipe.duration));
                }
                if (eutModifier != ContentModifier.IDENTITY) {
                    long preEUt = RecipeHelper.getRealEUt(recipe);
                    long eut = Math.max(1, eutModifier.apply(Math.abs(preEUt)));
                    EURecipeCapability.putEUContent(preEUt > 0 ? copied.tickInputs : copied.tickOutputs, eut);
                }
                return copied;
            };
        }
    }
}
