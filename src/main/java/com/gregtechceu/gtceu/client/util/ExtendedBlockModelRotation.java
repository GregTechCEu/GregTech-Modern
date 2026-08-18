package com.gregtechceu.gtceu.client.util;

import net.minecraft.core.Direction;

import lombok.Getter;

/**
 * All possible rotations for a fully orientable block.
 * <p>
 * This code is from
 * <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/forge/1.20.1/src/main/java/appeng/api/orientation/BlockOrientation.java">Applied
 * Energistics 2</a>,
 * licensed as LGPL 3.0.
 */
public enum ExtendedBlockModelRotation {

    DOWN_NORTH(90, 0, 0),
    DOWN_EAST(90, 0, 90),
    DOWN_SOUTH(90, 0, 180),
    DOWN_WEST(90, 0, 270),

    UP_SOUTH(270, 0, 0),
    UP_EAST(270, 0, 90),
    UP_NORTH(270, 0, 180),
    UP_WEST(270, 0, 270),

    NORTH_UP(0, 0, 0), // Default
    NORTH_EAST(0, 0, 90),
    NORTH_DOWN(0, 0, 180),
    NORTH_WEST(0, 0, 270),

    SOUTH_UP(0, 180, 0),
    SOUTH_WEST(0, 180, 90),
    SOUTH_DOWN(0, 180, 180),
    SOUTH_EAST(0, 180, 270),

    WEST_UP(0, 270, 0),
    WEST_NORTH(0, 270, 90),
    WEST_DOWN(0, 270, 180),
    WEST_SOUTH(0, 270, 270),

    EAST_UP(0, 90, 0),
    EAST_SOUTH(0, 90, 90),
    EAST_DOWN(0, 90, 180),
    EAST_NORTH(0, 90, 270);

    public static final ExtendedBlockModelRotation[] VALUES = values();

    @Getter
    private final int angleX;
    @Getter
    private final int angleY;
    @Getter
    private final int angleZ;

    ExtendedBlockModelRotation(int angleX, int angleY, int angleZ) {
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;
    }

    public static ExtendedBlockModelRotation get(Direction frontFacing) {
        return switch (frontFacing) {
            case NORTH -> NORTH_UP;
            case SOUTH -> SOUTH_UP;
            case EAST -> EAST_UP;
            case WEST -> WEST_UP;
            case UP -> UP_NORTH;
            case DOWN -> DOWN_NORTH;
        };
    }

    /**
     * Gets the block orientation in which the block's front and top are facing the specified directions.
     */
    public static ExtendedBlockModelRotation getExtended(Direction frontFacing, Direction upwardsFacing) {
        return switch (frontFacing) {
            case UP -> switch (upwardsFacing) {
                case NORTH -> UP_NORTH;
                case SOUTH -> UP_SOUTH;
                case WEST -> UP_WEST;
                case EAST -> UP_EAST;
                default -> NORTH_UP;
            };
            case DOWN -> switch (upwardsFacing) {
                case NORTH -> DOWN_NORTH;
                case SOUTH -> DOWN_SOUTH;
                case WEST -> DOWN_WEST;
                case EAST -> DOWN_EAST;
                default -> NORTH_UP;
            };
            case NORTH -> switch (upwardsFacing) {
                case UP -> NORTH_UP;
                case DOWN -> NORTH_DOWN;
                case WEST -> NORTH_WEST;
                case EAST -> NORTH_EAST;
                default -> NORTH_UP;
            };
            case SOUTH -> switch (upwardsFacing) {
                case UP -> SOUTH_UP;
                case DOWN -> SOUTH_DOWN;
                case WEST -> SOUTH_WEST;
                case EAST -> SOUTH_EAST;
                default -> NORTH_UP;
            };
            case WEST -> switch (upwardsFacing) {
                case UP -> WEST_UP;
                case DOWN -> WEST_DOWN;
                case NORTH -> WEST_NORTH;
                case SOUTH -> WEST_SOUTH;
                default -> NORTH_UP;
            };
            case EAST -> switch (upwardsFacing) {
                case UP -> EAST_UP;
                case DOWN -> EAST_DOWN;
                case NORTH -> EAST_NORTH;
                case SOUTH -> EAST_SOUTH;
                default -> NORTH_UP;
            };
        };
    }
}
