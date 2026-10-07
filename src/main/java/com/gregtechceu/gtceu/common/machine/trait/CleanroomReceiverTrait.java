package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;

import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class CleanroomReceiverTrait extends MachineTrait {

    @Setter
    protected @Nullable CleanroomProviderTrait cleanroomProvider;

    public CleanroomReceiverTrait() {
        cleanroomProvider = null;
    }

    public boolean hasActiveCleanroom(CleanroomType type) {
        return cleanroomProvider != null && cleanroomProvider.isActive() &&
                cleanroomProvider.getProvidedTypes().contains(type);
    }

    public void removeCleanroom() {
        cleanroomProvider = null;
    }
}
