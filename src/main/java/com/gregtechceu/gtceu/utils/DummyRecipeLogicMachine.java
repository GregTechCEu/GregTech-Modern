package com.gregtechceu.gtceu.utils;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeHandlerList;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.ActionResult;
import com.gregtechceu.gtceu.api.recipe.kind.GTRecipe;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.Collection;

/**
 * Dummy machine used for searching recipes outside a machine.
 */
public class DummyRecipeLogicMachine extends WorkableTieredMachine implements IRecipeLogicMachine {

    public DummyRecipeLogicMachine(IMachineBlockEntity be, int tier, Int2IntFunction tankScalingFunction,
                                   Collection<RecipeHandlerList> handlers,
                                   Object... args) {
        super(be, tier, tankScalingFunction, args);
        reinitializeHandlers(handlers);
    }

    public void reinitializeHandlers(Collection<RecipeHandlerList> handlers) {
        this.capabilitiesProxy.clear();
        this.capabilitiesFlat.clear();
        for (RecipeHandlerList handlerList : handlers) {
            this.addHandlerList(handlerList);
        }
    }

    @Override
    public DummyRecipeLogic getRecipeLogic() {
        return (DummyRecipeLogic) super.getRecipeLogic();
    }

    @Override
    protected RecipeLogic createRecipeLogic(Object... args) {
        return new DummyRecipeLogic(this);
    }

    /**
     * Wrapper recipe logic that exposes protected methods
     */
    public static class DummyRecipeLogic extends RecipeLogic {

        public DummyRecipeLogic(IRecipeLogicMachine machine) {
            super(machine);
        }

        @Override
        public ActionResult handleRecipeIO(GTRecipe recipe, IO io) {
            return super.handleRecipeIO(recipe, io);
        }
    }
}
