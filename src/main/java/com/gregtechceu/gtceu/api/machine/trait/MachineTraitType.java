package com.gregtechceu.gtceu.api.machine.trait;

import org.jetbrains.annotations.NotNull;

public final class MachineTraitType<T extends MachineTrait> {

    private final Class<T> clazz;
    private final boolean allowMultipleInstances;

    public MachineTraitType(@NotNull Class<T> clazz) {
        this(clazz, true);
    }

    public MachineTraitType(@NotNull Class<T> clazz, boolean allowMultipleInstances) {
        this.clazz = clazz;
        this.allowMultipleInstances = allowMultipleInstances;
    }

    public boolean allowsMultipleInstances() {
        return allowMultipleInstances;
    }

    public @NotNull T castTrait(@NotNull MachineTrait trait) {
        return clazz.cast(trait);
    }
}
