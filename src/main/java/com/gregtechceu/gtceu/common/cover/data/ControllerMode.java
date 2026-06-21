package com.gregtechceu.gtceu.common.cover.data;

import net.minecraft.core.Direction;

import lombok.Getter;

public enum ControllerMode {

    MACHINE("machine", null),
    COVER_UP("cover_up", Direction.UP),
    COVER_DOWN("cover_down", Direction.DOWN),
    COVER_NORTH("cover_north", Direction.NORTH),
    COVER_EAST("cover_east", Direction.EAST),
    COVER_SOUTH("cover_south", Direction.SOUTH),
    COVER_WEST("cover_west", Direction.WEST);

    @Getter
    public final String localeName;
    public final Direction side;

    ControllerMode(String localeName, Direction side) {
        this.localeName = localeName;
        this.side = side;
    }

    public String getTooltip() {
        return "cover.machine_controller.mode." + localeName;
    }

    public static final String nullLocaleName = "cover.machine_controller.mode.null";
}
