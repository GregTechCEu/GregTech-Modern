package com.gregtechceu.gtceu.api.machine.trait;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class MachineTraitType<T extends MachineTrait> {

    public final Class<T> traitCls;
    public final boolean allowMultiplePerMachine;

    public MachineTraitType(Class<T> cls) {
        this(cls, true);
    }

    public MachineTraitType(Class<T> cls, boolean allowMultiplePerMachine) {
        traitCls = cls;
        this.allowMultiplePerMachine = allowMultiplePerMachine;

        MACHINE_TRAIT_TYPES.put(this, cls);
    }

    private static final Map<MachineTraitType<?>, Class<?>> MACHINE_TRAIT_TYPES = new HashMap<>();

    public static <T extends MachineTrait> MachineTraitType<?> getTraitType(Class<T> cls) {
        var type = MACHINE_TRAIT_TYPES.entrySet().stream().filter(e -> e.getValue() == cls).findFirst();
        return type.orElseThrow().getKey();
    }
}
