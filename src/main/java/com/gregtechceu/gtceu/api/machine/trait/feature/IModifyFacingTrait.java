package com.gregtechceu.gtceu.api.machine.trait.feature;

import net.minecraft.core.Direction;

/// A machine trait that modifies the rotation behaviour of a machine.
public interface IModifyFacingTrait {

    default boolean isFacingValid(Direction direction) {
        return true;
    }
}
