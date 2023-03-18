package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote ICapabilityTrait
 */
public interface ICapabilityTrait {
    IO getCapabilityIO();

    default boolean canCapInput() {
        return getCapabilityIO() == IO.IN || getCapabilityIO() == IO.BOTH;
    }

    default boolean canCapOutput() {
        return getCapabilityIO() == IO.OUT || getCapabilityIO() == IO.BOTH;
    }
}
