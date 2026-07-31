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

    public MachineTraitType(Class<T> traitClass, @Nullable MachineTraitType<? super T> parentTraitType, boolean allowsMultipleInstances) {
        this.traitClass = traitClass;
        this.parentTraitType = parentTraitType;
        this.allowsMultipleInstances = allowsMultipleInstances;

        if (parentTraitType == null) {
            if (traitClass.getSuperclass() != MachineTrait.class) throw new IllegalStateException(("Error creating machine trait type for %s: Expected a parent trait type")
                    .formatted(traitClass));
        } else {
            if (traitClass.getSuperclass() == MachineTrait.class) {
                throw new IllegalStateException(("Error creating machine trait type for %s: Trait superclass is MachineTrait, but a parent trait type was provided").formatted(traitClass));
            } else if (!traitClass.getSuperclass().equals(parentTraitType.traitClass)) {
                throw new IllegalStateException(("Error creating machine trait type for %s: Trait superclass is %s, but parent trait type is for %s")
                        .formatted(traitClass, traitClass.getSuperclass(), parentTraitType.traitClass));
            }
        }

        if (parentTraitType != null && !parentTraitType.allowsMultipleInstances && allowsMultipleInstances) {
            throw new IllegalStateException(("Error creating machine trait type for %s: allowMultipleInstances is true, but the parent trait type does not allow multiple instances").formatted(traitClass));
        }
    }

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
