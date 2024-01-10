package com.gregtechceu.gtceu.api.capability.forge;

import com.gregtechceu.gtceu.api.capability.IPlatformEnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

public class GTEnergyHelperImpl {

    public static IPlatformEnergyStorage toPlatformEnergyStorage(IEnergyStorage handler) {
        return new IPlatformEnergyStorage() {
            @Override
            public long insert(long maxAmount, boolean simulate) {
                return handler.receiveEnergy((int) maxAmount, simulate);
            }

            @Override
            public long extract(long maxAmount, boolean simulate) {
                return handler.extractEnergy((int) maxAmount, simulate);
            }

            @Override
            public long getAmount() {
                return handler.getEnergyStored();
            }

            @Override
            public long getCapacity() {
                return handler.getMaxEnergyStored();
            }

            @Override
            public boolean supportsInsertion() {
                return handler.canReceive();
            }

            @Override
            public boolean supportsExtraction() {
                return handler.canExtract();
            }
        };
    }

    public static IEnergyStorage toEnergyStorage(IPlatformEnergyStorage energyStorage) {
        return new IEnergyStorage() {

            @Override
            public int receiveEnergy(int i, boolean bl) {
                return (int) Math.min(energyStorage.insert(i, bl), Integer.MAX_VALUE);
            }

            @Override
            public int extractEnergy(int i, boolean bl) {
                return (int) Math.min(energyStorage.extract(i, bl), Integer.MAX_VALUE);
            }

            @Override
            public int getEnergyStored() {
                return (int) Math.min(energyStorage.getAmount(), Integer.MAX_VALUE);
            }

            @Override
            public int getMaxEnergyStored() {
                return (int) Math.min(energyStorage.getCapacity(), Integer.MAX_VALUE);
            }

            @Override
            public boolean canExtract() {
                return energyStorage.supportsExtraction();
            }

            @Override
            public boolean canReceive() {
                return energyStorage.supportsInsertion();
            }
        };
    }
}
