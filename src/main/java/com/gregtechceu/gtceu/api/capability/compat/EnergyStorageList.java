package com.gregtechceu.gtceu.api.capability.compat;

import net.neoforged.neoforge.energy.IEnergyStorage;

import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class EnergyStorageList implements IEnergyStorage {

    public final IEnergyStorage[] storages;

    public EnergyStorageList(List<IEnergyStorage> storages) {
        this.storages = storages.toArray(IEnergyStorage[]::new);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int energyAdded = 0;
        for (IEnergyStorage iEnergyContainer : storages) {
            energyAdded += iEnergyContainer.receiveEnergy(maxReceive - energyAdded, simulate);
            if (energyAdded == maxReceive) {
                return energyAdded;
            }
        }
        return energyAdded;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int energyRemoved = maxExtract;
        for (IEnergyStorage iEnergyContainer : storages) {
            energyRemoved -= iEnergyContainer.extractEnergy(energyRemoved, simulate);
            if (energyRemoved == maxExtract) {
                return energyRemoved;
            }
        }
        return energyRemoved;
    }

    @Override
    public int getEnergyStored() {
        int energyStored = 0;
        for (IEnergyStorage energyStorage : storages) {
            energyStored += energyStorage.getEnergyStored();
        }
        return energyStored;
    }

    @Override
    public int getMaxEnergyStored() {
        int maxEnergyStored = 0;
        for (IEnergyStorage energyStorage : storages) {
            maxEnergyStored += energyStorage.getMaxEnergyStored();
        }
        return maxEnergyStored;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return true;
    }
}
