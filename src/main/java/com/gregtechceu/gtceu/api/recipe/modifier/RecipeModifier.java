package com.gregtechceu.gtceu.api.recipe.modifier;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import com.gregtechceu.gtceu.api.recipe.logic.OCParams;
import com.gregtechceu.gtceu.api.recipe.logic.OCResult;
import net.minecraft.MethodsReturnNonnullByDefault;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@FunctionalInterface
public interface RecipeModifier {

    @Nullable
    GTRecipe apply(MetaMachine machine, @NotNull GTRecipe recipe, @NotNull OCParams params, @NotNull OCResult result);
}
