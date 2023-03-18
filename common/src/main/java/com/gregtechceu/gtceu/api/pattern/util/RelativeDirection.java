package com.gregtechceu.gtceu.api.pattern.util;

import net.minecraft.core.Direction;

import java.util.function.Function;

/**
 * Relative direction when facing horizontally
 */
public enum RelativeDirection {
    UP(f -> Direction.UP, Direction.Axis.Y),
    DOWN(f -> Direction.DOWN, Direction.Axis.Y),
    LEFT(Direction::getCounterClockWise, Direction.Axis.X),
    RIGHT(Direction::getClockWise, Direction.Axis.X),
    FRONT(Function.identity(), Direction.Axis.Z),
    BACK(Direction::getOpposite, Direction.Axis.Z);

    final Function<Direction, Direction> actualFacing;
    public final Direction.Axis axis;

    RelativeDirection(Function<Direction, Direction> actualFacing, Direction.Axis axis) {
        this.actualFacing = actualFacing;
        this.axis = axis;
    }

    public Direction getActualFacing(Direction facing) {
        return actualFacing.apply(facing);
    }

    public boolean isSameAxis(RelativeDirection dir) {
        return this.axis == dir.axis;
    }

}
