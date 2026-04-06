package com.gregtechceu.gtceu.core;

public interface IBreathingEntity {

    default int gtceu$getOriginalMaxAirSupply() {
        throw new AssertionError("Mixin didn't apply");
    }

    default void gtceu$setMaxAirSupply(int newMaxAirSupply) {
        throw new AssertionError("Mixin didn't apply");
    }
}
