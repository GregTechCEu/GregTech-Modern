package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;

public class RecipeModifierList implements RecipeModifier {

    private final RecipeModifier[] modifiers;

    public RecipeModifierList(RecipeModifier... modifiers) {
        this.modifiers = modifiers;
    }

    @Override
    public @NotNull ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        ModifierFunction result = ModifierFunction.IDENTITY;
        var runningRecipe = recipe.copy();
        for (RecipeModifier modifier : modifiers) {
            var func = modifier.getModifier(machine, runningRecipe);
            runningRecipe = func.apply(runningRecipe);
            if (runningRecipe == null) return ModifierFunction.NULL;
            result = func.compose(result);

        }
        return result;
    }
}
