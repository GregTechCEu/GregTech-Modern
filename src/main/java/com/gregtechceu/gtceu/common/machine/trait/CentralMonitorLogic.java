package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.misc.EnergyContainerList;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.CentralMonitorMachine;

import net.minecraft.util.Mth;

public class CentralMonitorLogic extends RecipeLogic implements IWorkable {

    private static final int BASE_UPDATE_INTERVAL = 8 * 20;

    public CentralMonitorLogic(IRecipeLogicMachine machine) {
        super(machine);
    }

    public CentralMonitorMachine getMachine() {
        return (CentralMonitorMachine) machine;
    }

    private boolean consumeEnergy() {
        int tier = Mth.clamp(getMachine().getTier(), GTValues.ULV, GTValues.MAX);
        long energyToDrain = GTValues.VA[tier];
        EnergyContainerList energyContainer = getMachine().getFormedEnergyContainer();
        if (energyContainer == null) {
            return false;
        }

        long resultEnergy = energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            energyContainer.removeEnergy(energyToDrain);
            return true;
        }
        return false;
    }

    private int getUpdateInterval() {
        int interval = BASE_UPDATE_INTERVAL;
        for (int i = 1; i < getMachine().getTier(); i++) {
            interval /= 2;
        }
        return Math.max(interval, 1);
    }

    @Override
    public void serverTick() {
        if (!getMachine().isFormed() || !isWorkingEnabled()) {
            setStatus(Status.IDLE);
        } else if (consumeEnergy()) {
            setStatus(Status.WORKING);
            isActive = true;
            progress = (progress + 1) % getUpdateInterval();
            if (progress == 0) {
                getMachine().tick();
            }
        } else {
            setStatus(Status.WAITING);
            isActive = false;
            progress = Math.max(progress - 2, 1);
        }
    }

    @Override
    public int getMaxProgress() {
        return getUpdateInterval();
    }

    @Override
    public boolean isActive() {
        return getMachine().isFormed() && this.isActive;
    }
}
