package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import net.minecraft.world.level.block.state.BlockState;

import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public enum BoilerType {

    BRONZE(800, 1200,
            STEAM_MACHINE_CASING.getDefaultState(),
            BRONZE_FIREBOX.getDefaultState(),
            BRONZE_PIPE_CASING.getDefaultState()),

    STEEL(1800, 1800,
            SOLID_MACHINE_CASING.getDefaultState(),
            STEEL_FIREBOX.getDefaultState(),
            STEEL_PIPE_CASING.getDefaultState()),

    TITANIUM(3200, 2400,
            STABLE_MACHINE_CASING.getDefaultState(),
            TITANIUM_FIREBOX.getDefaultState(),
            TITANIUM_PIPE_CASING.getDefaultState()),

    TUNGSTENSTEEL(6400, 3000,
            ROBUST_MACHINE_CASING.getDefaultState(),
            TUNGSTENSTEEL_FIREBOX.getDefaultState(),
            TUNGSTENSTEEL_PIPE_CASING.getDefaultState());

    // Workable Data
    private final int steamPerTick;
    private final int ticksToBoiling;

    // Structure Data
    public final BlockState casingState;
    public final BlockState fireboxState;
    public final BlockState pipeState;

    BoilerType(int steamPerTick, int ticksToBoiling,
               BlockState casingState,
               BlockState fireboxState,
               BlockState pipeState) {

        this.steamPerTick = steamPerTick;
        this.ticksToBoiling = ticksToBoiling;

        this.casingState = casingState;
        this.fireboxState = fireboxState;
        this.pipeState = pipeState;
    }

    public int steamPerTick() {
        return steamPerTick;
    }

    public int getTicksToBoiling() {
        return ticksToBoiling;
    }

    public int runtimeBoost(int ticks) {
        return switch (this) {
            case BRONZE -> ticks * 2;
            case STEEL -> ticks * 150 / 100;
            case TITANIUM -> ticks * 120 / 100;
            case TUNGSTENSTEEL -> ticks;
        };
    }
}
