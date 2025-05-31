package com.gregtechceu.gtceu.api.machine.trait;

import com.gregtechceu.gtceu.api.capability.recipe.IO;

public interface ICapabilityTrait {

    IO getCapabilityIO();

    default boolean canCapInput() {
        return getCapabilityIO() == IO.IN || getCapabilityIO() == IO.BOTH;
    }

    default boolean canCapOutput() {
        return getCapabilityIO() == IO.OUT || getCapabilityIO() == IO.BOTH;
    }
}
