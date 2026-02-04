package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class CleanroomReceiverTrait extends MachineTrait {

    public static final MachineTraitType<CleanroomReceiverTrait> TYPE = new MachineTraitType<>(
            CleanroomReceiverTrait.class, false);

    @Setter
    protected @Nullable CleanroomProviderTrait cleanroomProvider;

    public CleanroomReceiverTrait(MetaMachine machine) {
        super(machine);
        cleanroomProvider = null;
    }

    @Override
    public MachineTraitType<CleanroomReceiverTrait> getTraitType() {
        return TYPE;
    }

    public boolean hasActiveCleanroom(CleanroomType type) {
        return cleanroomProvider != null && cleanroomProvider.isActive() &&
                cleanroomProvider.getProvidedTypes().contains(type);
    }

    public void removeCleanroom() {
        cleanroomProvider = null;
    }
}
