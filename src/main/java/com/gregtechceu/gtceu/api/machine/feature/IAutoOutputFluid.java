package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

public interface IAutoOutputFluid extends IMachineFeature {

    boolean isAutoOutputFluids();

    void setAutoOutputFluids(boolean allow);

    boolean isAllowInputFromOutputSideFluids();

    void setAllowInputFromOutputSideFluids(boolean allow);

    @Nullable
    Direction getOutputFacingFluids();

    void setOutputFacingFluids(@Nullable Direction outputFacing);

    default boolean hasAutoOutputFluid() {
        return true;
    }
}
