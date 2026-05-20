package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

import java.util.List;

public class RecipeAmperageEnergyContainer extends NotifiableEnergyContainer {

    public RecipeAmperageEnergyContainer(long maxCapacity,
                                         long maxInputVoltage, long maxInputAmperage,
                                         long maxOutputVoltage, long maxOutputAmperage) {
        super(maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
    }

    public static RecipeAmperageEnergyContainer makeEmitterContainer(long maxCapacity,
                                                                     long maxOutputVoltage, long maxOutputAmperage) {
        return new RecipeAmperageEnergyContainer(maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static RecipeAmperageEnergyContainer makeReceiverContainer(long maxCapacity,
                                                                      long maxInputVoltage, long maxInputAmperage) {
        return new RecipeAmperageEnergyContainer(maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    @Override
    protected List<Class<?>> validMachineClasses() {
        return List.of(IRecipeLogicMachine.class);
    }

    @Override
    public long getInputAmperage() {
        var recipeLogic = getMachine().getTrait(RecipeLogic.TYPE);
        if (recipeLogic == null) return 0;
        var lastRecipe = recipeLogic.getLastRecipe();
        long amperage;
        if (lastRecipe != null) {
            amperage = lastRecipe.getInputEUt().amperage();
        } else {
            amperage = super.getInputAmperage();
        }
        if (getEnergyCapacity() / 2 > getEnergyStored() && recipeLogic.isActive()) {
            return amperage + 1;
        } else {
            return amperage;
        }
    }

    @Override
    public long getOutputAmperage() {
        var recipeLogic = getMachine().getTrait(RecipeLogic.TYPE);
        if (recipeLogic == null) return 0;
        var lastRecipe = recipeLogic.getLastRecipe();
        if (lastRecipe != null) {
            return lastRecipe.getOutputEUt().amperage();
        } else {
            return super.getOutputAmperage();
        }
    }
}
