package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import lombok.Setter;

public class CleanroomRecieverTrait extends MachineTrait {
    public static MachineTraitType<CleanroomRecieverTrait> TYPE = new MachineTraitType<>(CleanroomRecieverTrait.class, false);

    @Override
    public MachineTraitType<CleanroomRecieverTrait> getTraitType() {
        return TYPE;
    }

    @Setter
    protected CleanroomProviderTrait cleanroomProvider;

    public CleanroomRecieverTrait(MetaMachine machine) {
        super(machine);
        cleanroomProvider = null;
    }

    public boolean hasActiveCleanroomProvider(CleanroomType type) {
        return cleanroomProvider.isActive() && cleanroomProvider.getProvidedTypes().contains(type);
    }
}
