package com.gregtechceu.gtceu.api.capability.fabric;

import com.gregtechceu.gtceu.api.capability.IPlatformEnergyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import team.reborn.energy.api.EnergyStorage;

public class GTEnergyHelperImpl {

    public static IPlatformEnergyStorage toPlatformEnergyStorage(EnergyStorage handler) {
        return new IPlatformEnergyStorage() {
            @Override
            public boolean supportsInsertion() {
                return handler.supportsInsertion();
            }

            @Override
            public long insert(long maxAmount, boolean simulate) {
                if (maxAmount == 0) return 0;
                long inserted = 0;
                try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                    inserted = handler.insert(maxAmount, transaction);
                    if (simulate) transaction.abort();
                    else transaction.commit();
                }
                return inserted;
            }

            @Override
            public boolean supportsExtraction() {
                return handler.supportsExtraction();
            }

            @Override
            public long extract(long maxAmount, boolean simulate) {
                if (maxAmount == 0) return 0;
                long extracted = 0;
                try (Transaction transaction = Transaction.openNested(Transaction.getCurrentUnsafe())) {
                    extracted = handler.extract(maxAmount, transaction);
                    if (simulate) transaction.abort();
                    else transaction.commit();
                }
                return extracted;
            }

            @Override
            public long getAmount() {
                return handler.getAmount();
            }

            @Override
            public long getCapacity() {
                return handler.getCapacity();
            }
        };
    }

    public static EnergyStorage toEnergyStorage(IPlatformEnergyStorage energyStorage) {
        return new EnergyStorage() {

            @Override
            public boolean supportsInsertion() {
                return energyStorage.supportsInsertion();
            }

            @Override
            public long insert(long maxAmount, TransactionContext transaction) {
                return energyStorage.insert(maxAmount, false);
            }

            @Override
            public boolean supportsExtraction() {
                return energyStorage.supportsExtraction();
            }

            @Override
            public long extract(long maxAmount, TransactionContext transaction) {
                return energyStorage.extract(maxAmount, false);
            }

            @Override
            public long getAmount() {
                return energyStorage.getAmount();
            }

            @Override
            public long getCapacity() {
                return energyStorage.getCapacity();
            }
        };
    }
}
