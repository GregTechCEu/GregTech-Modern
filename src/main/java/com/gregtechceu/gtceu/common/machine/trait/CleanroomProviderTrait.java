package com.gregtechceu.gtceu.common.machine.trait;

import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.machine.trait.MachineTrait;
import com.gregtechceu.gtceu.api.machine.trait.MachineTraitType;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

public class CleanroomProviderTrait extends MachineTrait {

    public static final MachineTraitType<CleanroomProviderTrait> TYPE = new MachineTraitType<>(
            CleanroomProviderTrait.class, false);

    @Getter
    @Setter
    private Set<CleanroomType> providedTypes;
    @Getter
    @Setter
    private boolean isActive;

    public CleanroomProviderTrait() {
        this(Set.of(CleanroomType.CLEANROOM));
    }

    public CleanroomProviderTrait(Set<CleanroomType> providedTypes) {
        super();
        this.providedTypes = new ObjectOpenHashSet<>(providedTypes);
        this.isActive = false;
    }

    @Override
    public MachineTraitType<CleanroomProviderTrait> getTraitType() {
        return TYPE;
    }
}
