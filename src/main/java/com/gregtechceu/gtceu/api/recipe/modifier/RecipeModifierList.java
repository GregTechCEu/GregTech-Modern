package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import lombok.Getter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a list of RecipeModifiers that should be applied in order
 */
public final class RecipeModifierList implements RecipeModifier {

    @Getter
    private final RecipeModifier[] modifiers;

    public RecipeModifierList(RecipeModifier... modifiers) {
        this.modifiers = modifiers;
    }

    /**
     * Builds the final ModifierFunction by applying each RecipeModifier in order
     * <p>
     * The RecipeModifierList will build modifiers by keeping tracking of the recipe as each modifier is applied
     * </p>
     *
     * @param machine the machine which is requesting the modifier
     * @param recipe  the recipe - will not be mutated
     * @return Fully composed ModifierFunction of all desired RecipeModifiers
     */
    @Override
    @Contract(pure = true)
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        ModifierFunction result = ModifierFunction.IDENTITY;
        var runningRecipe = recipe;
        for (RecipeModifier modifier : modifiers) {
            var func = modifier.getModifier(machine, runningRecipe);
            runningRecipe = func.apply(runningRecipe);
            if (runningRecipe == null) return ModifierFunction.NULL;
            result = func.compose(result);
        }
        return result;
    }
}
