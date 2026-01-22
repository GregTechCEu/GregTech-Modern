package com.gregtechceu.gtceu.api.machine.trait.feature;

import net.minecraft.core.Direction;

/**
 * A machine trait that modifies the rotation behaviour of a machine.
 */
public interface IFrontFacingTrait {

    /**
     * Returns if a machine can be rotated to be facing the given direction.
     */
    default boolean isValidFrontFace(Direction direction) {
        return true;
    }
}
