package com.gregtechceu.gtceu.common.machine.multiblock.electric;

import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;

import java.util.Objects;

public class FusionReactorMachine extends WorkableElectricMultiblockMachine {

    private EnergyContainerList inputEnergyContainers;

    public FusionReactorMachine(IMachineBlockEntity holder) {
        super(holder);
    }

    @Override
    public void onStructureFormed() {
        if (this.inputEnergyContainers.getEnergyStored() > 0) {
            long energyAdded = this.energyContainer.addEnergy(this.inputEnergyContainers.getEnergyStored());
            if (energyAdded > 0) this.inputEnergyContainers.removeEnergy(energyAdded);
        }
        super.onStructureFormed();
        if (recipeMapWorkable.isWorking() && color == null) {
            if (recipeMapWorkable.getPreviousRecipe() != null && recipeMapWorkable.getPreviousRecipe().getFluidOutputs().size() > 0) {
                int newColor = 0xFF000000 | recipeMapWorkable.getPreviousRecipe().getFluidOutputs().get(0).getFluid().getColor();
                if (!Objects.equals(color, newColor)) {
                    color = newColor;
                    writeCustomData(GregtechDataCodes.UPDATE_COLOR, this::writeColor);
                }
            }
        } else if (!recipeMapWorkable.isWorking() && isStructureFormed() && color != null) {
            color = null;
            writeCustomData(GregtechDataCodes.UPDATE_COLOR, this::writeColor);
        }
    }

}
