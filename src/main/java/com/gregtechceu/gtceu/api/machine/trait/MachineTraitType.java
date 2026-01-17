package com.gregtechceu.gtceu.api.machine.trait;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MachineTraitType<T extends MachineTrait> {

    private static final Map<Class<? extends MachineTrait>, MachineTraitType<?>> MACHINE_TRAIT_TYPES = new HashMap<>();

    private final Class<T> clazz;
    private final boolean allowMultipleInstances;

    public MachineTraitType(@NotNull Class<T> clazz) {
        this(clazz, true);
    }

    public MachineTraitType(@NotNull Class<T> clazz, boolean allowMultipleInstances) {
        this.clazz = clazz;
        this.allowMultipleInstances = allowMultipleInstances;

        MACHINE_TRAIT_TYPES.put(clazz, this);
    }

    public boolean allowsMultipleInstances() {
        return allowMultipleInstances;
    }

    public T castTrait(MachineTrait trait) {
        return clazz.cast(trait);
    }

    public static <T extends MachineTrait> MachineTraitType<?> getTraitType(Class<T> cls) {
        return MACHINE_TRAIT_TYPES.get(cls);
    }
}
