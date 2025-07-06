package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;

public class RecipeAmperageEnergyContainer extends NotifiableEnergyContainer {

    protected final IRecipeLogicMachine machine;

    public RecipeAmperageEnergyContainer(IRecipeLogicMachine machine, long maxCapacity,
                                         long maxInputVoltage, long maxInputAmperage,
                                         long maxOutputVoltage, long maxOutputAmperage) {
        super(machine.self(), maxCapacity, maxInputVoltage, maxInputAmperage, maxOutputVoltage, maxOutputAmperage);
        this.machine = machine;
    }

    public static RecipeAmperageEnergyContainer makeEmitterContainer(IRecipeLogicMachine machine, long maxCapacity,
                                                                     long maxOutputVoltage, long maxOutputAmperage) {
        return new RecipeAmperageEnergyContainer(machine, maxCapacity, 0L, 0L, maxOutputVoltage, maxOutputAmperage);
    }

    public static RecipeAmperageEnergyContainer makeReceiverContainer(IRecipeLogicMachine machine, long maxCapacity,
                                                                      long maxInputVoltage, long maxInputAmperage) {
        return new RecipeAmperageEnergyContainer(machine, maxCapacity, maxInputVoltage, maxInputAmperage, 0L, 0L);
    }

    @Override
    public long getInputAmperage() {
        var lastRecipe = machine.getRecipeLogic().getLastRecipe();
        long amperage;
        if (lastRecipe != null) {
            amperage = lastRecipe.getInputEUt().amperage();
        } else {
            amperage = super.getInputAmperage();
        }
        if (getEnergyCapacity() / 2 > getEnergyStored() && machine.getRecipeLogic().isActive()) {
            return amperage + 1;
        } else {
            return amperage;
        }
    }

    @Override
    public long getOutputAmperage() {
        var lastRecipe = machine.getRecipeLogic().getLastRecipe();
        if (lastRecipe != null) {
            return lastRecipe.getOutputEUt().amperage();
        } else {
            return super.getOutputAmperage();
        }
    }
}
