package com.gregtechceu.gtceu.common.machine.multiblock.steam;


import com.gregtechceu.gtceu.common.block.variant.BoilerFireBoxCasingBlock;
import net.minecraft.world.level.block.state.BlockState;

import static com.gregtechceu.gtceu.common.block.variant.CasingBlock.CasingType.*;
import static com.gregtechceu.gtceu.common.libs.GTBlocks.*;

public enum BoilerType {

    BRONZE(800, 1200,
            CASING.get().getState(BRONZE_BRICKS),
            BOILER_FIREBOX_CASING.get().getState(BoilerFireBoxCasingBlock.CasingType.BRONZE_FIREBOX),
            CASING.get().getState(BRONZE_PIPE)),

    STEEL(1800, 1800,
            CASING.get().getState(STEEL_SOLID),
            BOILER_FIREBOX_CASING.get().getState(BoilerFireBoxCasingBlock.CasingType.STEEL_FIREBOX),
            CASING.get().getState(STEEL_PIPE)),

    TITANIUM(3200, 2400,
            CASING.get().getState(TITANIUM_STABLE),
            BOILER_FIREBOX_CASING.get().getState(BoilerFireBoxCasingBlock.CasingType.TITANIUM_FIREBOX),
            CASING.get().getState(TITANIUM_PIPE)),

    TUNGSTENSTEEL(6400, 3000,
            CASING.get().getState(TUNGSTENSTEEL_ROBUST),
            BOILER_FIREBOX_CASING.get().getState(BoilerFireBoxCasingBlock.CasingType.TUNGSTENSTEEL_FIREBOX),
            CASING.get().getState(TUNGSTENSTEEL_PIPE));

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
