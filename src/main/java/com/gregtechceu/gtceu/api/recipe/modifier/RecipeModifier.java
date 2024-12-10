package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface RecipeModifier {

    RecipeModifier NO_MODIFIER = (m, r) -> ModifierFunction.IDENTITY;

    @Contract(pure = true)
    @NotNull
    ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe);
}
