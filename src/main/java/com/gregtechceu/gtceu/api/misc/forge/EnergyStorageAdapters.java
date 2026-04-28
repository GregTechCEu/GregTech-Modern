package com.gregtechceu.gtceu.api.misc.forge;

import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class EnergyStorageAdapters {

    private EnergyStorageAdapters() {}

    public static EnergyHandler toEnergyHandler(IEnergyStorage storage) {
        return storage == null ? null : new LegacyToEnergy(storage);
    }

    public static IEnergyStorage toEnergyStorage(EnergyHandler handler) {
        return handler == null ? null : IEnergyStorage.of(handler);
    }

    private record LegacyToEnergy(IEnergyStorage storage) implements EnergyHandler {

        @Override
        public long getAmountAsLong() {
            return storage.getEnergyStored();
        }

        @Override
        public long getCapacityAsLong() {
            return storage.getMaxEnergyStored();
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            return amount <= 0 ? 0 : storage.receiveEnergy(amount, false);
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            return amount <= 0 ? 0 : storage.extractEnergy(amount, false);
        }
    }
}
