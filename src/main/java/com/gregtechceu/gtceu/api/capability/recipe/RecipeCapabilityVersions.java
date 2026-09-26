package com.gregtechceu.gtceu.api.capability.recipe;

import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.machine.trait.recipe.RecipeHandlerList;

import java.util.List;
import java.util.Map;

public final class RecipeCapabilityVersions {

    public int inputVersion;
    public int outputVersion;
    public int topologyVersion;

    public int cachedGroupsVersionIn = -1;
    public int cachedGroupsVersionOut = -1;
    public Map<RecipeHandlerGroup, List<RecipeHandlerList>> cachedGroupsIn;
    public Map<RecipeHandlerGroup, List<RecipeHandlerList>> cachedGroupsOut;
}
