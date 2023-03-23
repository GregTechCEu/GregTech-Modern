package com.gregtechceu.gtceu.common.machine.multiblock.steam;

import net.minecraft.world.level.block.state.BlockState;

import static com.gregtechceu.gtceu.common.data.GTBlocks.*;

public enum BoilerType {

    BRONZE(800, 1200,
            CASING_BRONZE_BRICKS.getDefaultState(),
            FIREBOX_BRONZE.getDefaultState(),
            CASING_BRONZE_PIPE.getDefaultState()),

    STEEL(1800, 1800,
            CASING_STEEL_SOLID.getDefaultState(),
            FIREBOX_STEEL.getDefaultState(),
            CASING_STEEL_PIPE.getDefaultState()),

    TITANIUM(3200, 2400,
            CASING_TITANIUM_STABLE.getDefaultState(),
            FIREBOX_TITANIUM.getDefaultState(),
            CASING_TITANIUM_PIPE.getDefaultState()),

    TUNGSTENSTEEL(6400, 3000,
            CASING_TUNGSTENSTEEL_ROBUST.getDefaultState(),
            FIREBOX_TUNGSTENSTEEL.getDefaultState(),
            CASING_TUNGSTENSTEEL_PIPE.getDefaultState());

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
