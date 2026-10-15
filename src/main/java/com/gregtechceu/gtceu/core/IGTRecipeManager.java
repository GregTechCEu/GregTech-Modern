package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.api.recipe.ingredient.FluidIngredient;

import it.unimi.dsi.fastutil.objects.Object2IntMap;

public interface IGTRecipeManager {

    Object2IntMap<FluidIngredient> gtceu$getJetpackFuels();
}
