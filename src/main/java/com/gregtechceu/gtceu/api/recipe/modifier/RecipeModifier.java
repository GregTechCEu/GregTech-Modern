package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machines.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@FunctionalInterface
public interface RecipeModifier {
    @Nullable
    GTRecipe apply(MetaMachine machine, GTRecipe recipe);
}
