package com.gregtechceu.gtceu.api.machine.trait;

public final class MachineTraitType<T extends MachineTrait> {

    private final Class<T> clazz;
    private final boolean allowMultipleInstances;

    public MachineTraitType(Class<T> clazz) {
        this(clazz, true);
    }

    public MachineTraitType(Class<T> clazz, boolean allowMultipleInstances) {
        this.clazz = clazz;
        this.allowMultipleInstances = allowMultipleInstances;
    }

    public boolean allowsMultipleInstances() {
        return allowMultipleInstances;
    }

    public T castTrait(MachineTrait trait) {
        return clazz.cast(trait);
    }
}
