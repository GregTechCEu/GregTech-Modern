package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface RecipeModifier {

    @Nullable
    @Contract(pure = true)
    ModifierFunction getModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe);
}
