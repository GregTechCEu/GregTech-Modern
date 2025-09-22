package com.gregtechceu.gtceu.api.machine.feature;

import net.minecraft.core.Direction;

import org.jetbrains.annotations.Nullable;

public interface IAutoOutputItem extends IMachineFeature {

    boolean isAutoOutputItems();

    void setAutoOutputItems(boolean allow);

    boolean isAllowInputFromOutputSideItems();

    void setAllowInputFromOutputSideItems(boolean allow);

    @Nullable
    Direction getOutputFacingItems();

    void setOutputFacingItems(@Nullable Direction outputFacing);

    default boolean hasAutoOutputItem() {
        return true;
    }
}
