package com.gregtechceu.gtceu.api.fluids;

import lombok.Getter;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import org.jetbrains.annotations.NotNull;

public enum FluidState {

    LIQUID("gtceu.fluid.state_liquid"),
    GAS("gtceu.fluid.state_gas"),
    PLASMA("gtceu.fluid.state_plasma"),
    ;

    @Getter
    private final String translationKey;

    FluidState(@NotNull String translationKey) {
        this.translationKey = translationKey;
    }

    public static FluidState inferState(FluidStack stack) {
        if (stack.getFluid() instanceof GTFluid fluid) return fluid.getState();
        else return stack.getFluid().getFluidType().isLighterThanAir() ? GAS : LIQUID;
    }
}
