package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class CleanroomProviderTrait extends MachineTrait {
    public static MachineTraitType<CleanroomProviderTrait> TYPE = new MachineTraitType<>(CleanroomProviderTrait.class, false);

    @Override
    public MachineTraitType<CleanroomProviderTrait> getTraitType() {
        return TYPE;
    }

    private final Set<CleanroomType> providedTypes = new HashSet<>();
    @Getter
    @Setter
    private boolean isActive;
    public CleanroomProviderTrait(MetaMachine machine, Set<CleanroomType> providedTypes) {
        super(machine);
        this.providedTypes.addAll(providedTypes);
        this.isActive = false;
    }

    public CleanroomProviderTrait(MetaMachine machine) {
        this(machine, Set.of(CleanroomType.CLEANROOM));
    }

    public @Unmodifiable Set<CleanroomType> getProvidedTypes() {
        return Collections.unmodifiableSet(providedTypes);
    }

    public void addProvidedType(CleanroomType type) {
        providedTypes.add(type);
    }

    public void removeProvidedType(CleanroomType type) {
        providedTypes.remove(type);
    }

    public void clearProvidedTypes() {
        providedTypes.clear();
    }
}
