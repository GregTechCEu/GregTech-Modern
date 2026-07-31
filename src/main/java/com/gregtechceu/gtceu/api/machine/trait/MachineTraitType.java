package com.gregtechceu.gtceu.api.machine.trait;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the type of a machine trait, can be used to query a machine for traits of this type.
 * @param traitClass The class of this machine trait
 * @param parentTraitType The parent trait type (or null if this trait is a direct subclass of {@link MachineTrait}
 * @param allowsMultipleInstances If this trait allows multiple instances to be attached to one machine (default true)
 */
public record MachineTraitType<T extends MachineTrait>(Class<T> traitClass,
                                                       @Nullable MachineTraitType<? super T> parentTraitType,
                                                       boolean allowsMultipleInstances) {

    /**
     * @param traitClass The class of this machine trait
     * @param parentTraitType The parent trait type (or null if this trait is a direct subclass of {@link MachineTrait}
     */
    public MachineTraitType(Class<T> traitClass, @Nullable MachineTraitType<? super T> parentTraitType) {
        this(traitClass, parentTraitType, true);
    }


    public T castTrait(MachineTrait trait) {
        return traitClass.cast(trait);
    }
}
