package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;

import java.util.function.Consumer;

@FunctionalInterface
public interface MachineInstanceFactory<T extends MetaMachine> {

    T buildMachine(BlockEntityCreationInfo info);

    default MachineInstanceFactory<T> andThen(Consumer<T> modifier) {
        return (info) -> {
            T machine = this.buildMachine(info);
            modifier.accept(machine);
            return machine;
        };
    }

    @FunctionalInterface
    interface Tiered<T extends MetaMachine> {

        T buildMachine(BlockEntityCreationInfo info, int tier);

        default Tiered<T> andThen(Consumer<T> modifier) {
            return (info, tier) -> {
                T machine = buildMachine(info, tier);
                modifier.accept(machine);
                return machine;
            };
        }
    }

    @FunctionalInterface
    interface Steam<T extends MetaMachine> {

        T buildMachine(BlockEntityCreationInfo info, boolean isHighPressure);
    }
}
