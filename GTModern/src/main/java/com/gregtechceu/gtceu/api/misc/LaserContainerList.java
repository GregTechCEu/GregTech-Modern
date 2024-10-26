package com.gregtechceu.gtceu.api.misc;

import com.gregtechceu.gtceu.api.capability.ILaserContainer;

import net.minecraft.core.Direction;

import java.util.List;

public class LaserContainerList implements ILaserContainer {

    private final List<? extends ILaserContainer> energyContainerList;

    public LaserContainerList(List<? extends ILaserContainer> energyContainerList) {
        this.energyContainerList = energyContainerList;
    }

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage) {
        long amperesUsed = 0L;
        List<? extends ILaserContainer> energyContainerList = this.energyContainerList;
        for (ILaserContainer iEnergyContainer : energyContainerList) {
            amperesUsed += iEnergyContainer.acceptEnergyFromNetwork(null, voltage, amperage);
            if (amperage == amperesUsed) {
                return amperesUsed;
            }
        }
        return amperesUsed;
    }

    @Override
    public long changeEnergy(long energyToAdd) {
        long energyAdded = 0L;
        List<? extends ILaserContainer> energyContainerList = this.energyContainerList;
        for (ILaserContainer iEnergyContainer : energyContainerList) {
            energyAdded += iEnergyContainer.changeEnergy(energyToAdd - energyAdded);
            if (energyAdded == energyToAdd) {
                return energyAdded;
            }
        }
        return energyAdded;
    }

    @Override
    public long getEnergyStored() {
        long energyStored = 0L;
        for (ILaserContainer iEnergyContainer : energyContainerList) {
            energyStored += iEnergyContainer.getEnergyStored();
        }
        return energyStored;
    }

    @Override
    public long getEnergyCapacity() {
        long energyCapacity = 0L;
        for (ILaserContainer iEnergyContainer : energyContainerList) {
            energyCapacity += iEnergyContainer.getEnergyCapacity();
        }
        return energyCapacity;
    }

    @Override
    public long getInputAmperage() {
        return 1L;
    }

    @Override
    public long getOutputAmperage() {
        return 1L;
    }

    @Override
    public long getInputVoltage() {
        long inputVoltage = 0L;
        for (ILaserContainer container : energyContainerList) {
            inputVoltage += container.getInputVoltage() * container.getInputAmperage();
        }
        return inputVoltage;
    }

    @Override
    public long getOutputVoltage() {
        long outputVoltage = 0L;
        for (ILaserContainer container : energyContainerList) {
            outputVoltage += container.getOutputVoltage() * container.getOutputAmperage();
        }
        return outputVoltage;
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return true;
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return true;
    }
}
