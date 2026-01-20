package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import lombok.Getter;
import lombok.Setter;
import java.util.HashSet;
import java.util.Set;

public class CleanroomProviderTrait extends MachineTrait {
    public static MachineTraitType<CleanroomProviderTrait> TYPE = new MachineTraitType<>(CleanroomProviderTrait.class, false);

    @Override
    public MachineTraitType<CleanroomProviderTrait> getTraitType() {
        return TYPE;
    }

    @Getter
    @Setter
    private Set<CleanroomType> providedTypes = new HashSet<>();
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
}
