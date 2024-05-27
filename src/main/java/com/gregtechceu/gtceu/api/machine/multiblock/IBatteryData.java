package com.gregtechceu.gtceu.api.machine.multiblock;

import org.jetbrains.annotations.NotNull;

public interface IBatteryData {

    int getTier();

    long getCapacity();

    @NotNull
    String getBatteryName();
}
