package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.IRecipeHandler;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IgnoreEnergyRecipeHandler implements IRecipeHandler<Long> {

    @Override
    public List<Long> handleRecipeInner(IO io, GTRecipe recipe, List<Long> left, boolean simulate) {
        return null;
    }

    @Override
    public @NotNull List<Object> getContents() {
        return List.of(Long.MAX_VALUE);
    }

    @Override
    public double getTotalContentAmount() {
        return Long.MAX_VALUE;
    }

    @Override
    public RecipeCapability<Long> getCapability() {
        return EURecipeCapability.CAP;
    }
}
