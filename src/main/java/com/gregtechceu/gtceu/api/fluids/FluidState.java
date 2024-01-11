package com.gregtechceu.gtceu.api.fluids;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public enum FluidState {
    LIQUID("gtceu.fluid.state_liquid"),
    GAS("gtceu.fluid.state_gas"),
    PLASMA("gtceu.fluid.state_plasma"),
    MOLTEN("gtceu.fluid.state_molten"),
    ;

    @Getter
    private final String translationKey;

    FluidState(@NotNull String translationKey) {
        this.translationKey = translationKey;
    }
}
