package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class CleanroomRecieverTrait extends MachineTrait {

    public static final MachineTraitType<CleanroomRecieverTrait> TYPE = new MachineTraitType<>(CleanroomRecieverTrait.class,
            false);

    @Override
    public MachineTraitType<CleanroomRecieverTrait> getTraitType() {
        return TYPE;
    }

    @Setter
    protected @Nullable CleanroomProviderTrait cleanroomProvider;

    public CleanroomRecieverTrait(MetaMachine machine) {
        super(machine);
        cleanroomProvider = null;
    }

    public boolean hasActiveCleanroomProvider(CleanroomType type) {
        return cleanroomProvider != null && cleanroomProvider.isActive() && cleanroomProvider.getProvidedTypes().contains(type);
    }

    public void removeCleanroomProvider() {
        cleanroomProvider = null;
    }
}
