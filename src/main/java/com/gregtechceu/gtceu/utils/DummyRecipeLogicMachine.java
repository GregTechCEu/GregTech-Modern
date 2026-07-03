package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.RecipeTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerGroup;
import com.gregtechceu.gtceu.api.recipe.handler.RecipeHandlerList;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Dummy machine used for searching recipes outside of a machine.
 */
public class DummyRecipeLogicMachine extends RecipeTieredMachine implements IRecipeLogicMachine {

    RecipeHandlerGroup recipeHandlerGroup;

    public DummyRecipeLogicMachine(IMachineBlockEntity be, int tier, Int2IntFunction tankScalingFunction,
                                   RecipeHandlerGroup group) {
        super(be, tier, tankScalingFunction);
        reinitializeHandlers(group);
    }

    public void reinitializeHandlers(RecipeHandlerGroup group) {
        recipeHandlerGroup = group;
    }

    @Override
    public @NotNull List<RecipeHandlerList> getRecipeHandlerLists() {
        return List.of();
    }

    @Override
    public List<RecipeHandlerGroup> getRecipeHandlerGroups() {
        return List.of(recipeHandlerGroup);
    }
}
