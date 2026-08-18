package com.gregtechceu.gtceu.utils.data;

import net.minecraft.core.Direction;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

// TODO convert into datafixers
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagCompatibilityFixer {

    public static Direction fixUpwardsFacing(Direction frontfacing, Direction upwardsFacing) {
        Direction newUpwards;
        switch (frontfacing) {
            case NORTH: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.WEST;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.EAST;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case EAST: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.NORTH;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.SOUTH;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case SOUTH: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.EAST;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.WEST;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case WEST: {
                if (upwardsFacing == Direction.NORTH) {
                    newUpwards = Direction.UP;
                } else if (upwardsFacing == Direction.EAST) {
                    newUpwards = Direction.SOUTH;
                } else if (upwardsFacing == Direction.SOUTH) {
                    newUpwards = Direction.DOWN;
                } else if (upwardsFacing == Direction.WEST) {
                    newUpwards = Direction.NORTH;
                } else {
                    newUpwards = Direction.UP;
                }
                break;
            }
            case DOWN:
            case UP:
            default: {
                newUpwards = upwardsFacing;
                // needed if previous machine did not have an upwards facing
                if (upwardsFacing == null) {
                    newUpwards = Direction.NORTH;
                }
            }
        }
        return newUpwards;
    }
}
