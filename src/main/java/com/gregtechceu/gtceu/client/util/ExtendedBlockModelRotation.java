package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.data.RotationState;
import com.gregtechceu.gtceu.api.pattern.util.RelativeDirection;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.math.Transformation;
import lombok.Getter;
import org.joml.Quaternionf;

import java.util.EnumSet;
import java.util.Set;

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

    // DUNSWE -> NSWE
    // @formatter:off
    DOWN_NORTH(90, 0, 0, 0),
    DOWN_SOUTH(90, 0, 270, 1),
    DOWN_WEST(90, 0, 180, 2),
    DOWN_EAST(90, 0, 90, 3),

    UP_NORTH(270, 0, 180, 0),
    UP_SOUTH(270, 0, 90, 1),
    UP_WEST(270, 0, 0, 2),
    UP_EAST(270, 0, 270, 3),

    NORTH_NORTH(0, 0, 0, 0), // Default,
    NORTH_SOUTH(0, 0, 270, 1),
    NORTH_WEST(0, 0, 180, 2),
    NORTH_EAST(0, 0, 90, 3),

    SOUTH_NORTH(0, 180, 0, 0),
    SOUTH_SOUTH(0, 180, 90, 1),
    SOUTH_WEST(0, 180, 180, 2),
    SOUTH_EAST(0, 180, 270, 3),

    WEST_NORTH(0, 270, 0, 0),
    WEST_SOUTH(0, 270, 270, 1),
    WEST_WEST(0, 270, 180, 2),
    WEST_EAST(0, 270, 90, 3),

    EAST_NORTH(0, 90, 0, 0),
    EAST_SOUTH(0, 90, 270, 1),
    EAST_WEST(0, 90, 180, 2),
    EAST_EAST(0, 90, 90, 3);
    // @formatter:on

    public static final ExtendedBlockModelRotation[] VALUES = values();

    @Getter
    private final int angleX;
    @Getter
    private final int angleY;
    @Getter
    private final int angleZ;
    @Getter
    private final Quaternionf quaternion;
    @Getter
    private final Transformation transformation;
    /**
     * How many times it has been rotated clock-wise around in 90° increments around its facing.
     */
    @Getter
    private final int spin;
    // Map each Direction to the Direction it'll be rotated to
    private final Direction[] rotatedSideTo;
    // Reverse of rotatedSideTo
    private final Direction[] rotatedSideFrom;

    ExtendedBlockModelRotation(int angleX, int angleY, int angleZ, int spin) {
        this.angleX = angleX;
        this.angleY = angleY;
        this.angleZ = angleZ;

        // NOTE: Mojang's block model rotation rotates in the opposite direction
        quaternion = new Quaternionf().rotateYXZ(
                -angleY * Mth.DEG_TO_RAD,
                -angleX * Mth.DEG_TO_RAD,
                -angleZ * Mth.DEG_TO_RAD);

        if (angleX == 0 && angleY == 0 && angleZ == 0) {
            this.transformation = Transformation.identity();
        } else {
            this.transformation = new Transformation(null, quaternion, null, null);
        }
        this.spin = spin;

        // Build a mapping between the sides in this orientation
        this.rotatedSideTo = new Direction[GTUtil.DIRECTIONS.length];
        this.rotatedSideFrom = new Direction[GTUtil.DIRECTIONS.length];
        for (var direction : GTUtil.DIRECTIONS) {
            var normal = direction.step();
            normal.rotate(quaternion);
            var rotatedTo = Direction.getNearest(normal.x(), normal.y(), normal.z());
            rotatedSideTo[direction.ordinal()] = rotatedTo;
            rotatedSideFrom[rotatedTo.ordinal()] = direction;
        }
    }

    public boolean isRedundant() {
        return angleX == 0 && angleY == 0 && angleZ == 0;
    }

    public Direction rotate(Direction direction) {
        return rotatedSideTo[direction.ordinal()];
    }

    public Direction resultingRotate(Direction direction) {
        return rotatedSideFrom[direction.ordinal()];
    }

    public static ExtendedBlockModelRotation get(Direction direction) {
        return get(direction, Direction.NORTH);
    }

    /**
     * Gets the block orientation in which the block's front and top are facing the specified directions.
     */
    public static ExtendedBlockModelRotation get(Direction direction, Direction up) {
        // first valid upwards facing is NORTH (ordinal 2), so we substract 2 to index
        return VALUES[direction.ordinal() * 4 + (up.ordinal() - 2)];
    }

    public static ExtendedBlockModelRotation get(BlockState state) {
        RotationState rotationState = RotationState.NONE;
        for (var r : RotationState.values()) {
            if (state.hasProperty(r.property)) {
                rotationState = r;
                break;
            }
        }
        if (rotationState == RotationState.NONE) {
            return NORTH_NORTH;
        }
        return get(rotationState, state);
    }

    public static ExtendedBlockModelRotation get(RotationState rotationState, BlockState state) {
        var direction = state.getValue(rotationState.property);
        var spin = state.hasProperty(GTBlockStateProperties.UPWARDS_FACING) ?
                state.getValue(GTBlockStateProperties.UPWARDS_FACING) :
                Direction.NORTH;
        return get(direction, spin);
    }

    public Direction getDirection(RelativeDirection direction) {
        return rotate(direction.global);
    }

    public RelativeDirection getRelativeDirection(Direction side) {
        return RelativeDirection.fromGlobalDirection(resultingRotate(side));
    }

    public Set<Direction> getSides(Set<RelativeDirection> relativeSides) {
        var result = EnumSet.noneOf(Direction.class);
        for (var relativeSide : relativeSides) {
            result.add(getDirection(relativeSide));
        }
        return result;
    }

    public Set<RelativeDirection> getRelativeDirections(Set<Direction> sides) {
        var result = EnumSet.noneOf(RelativeDirection.class);
        for (var side : sides) {
            result.add(getRelativeDirection(side));
        }
        return result;
    }

    public ExtendedBlockModelRotation rotateClockwiseAround(Direction side) {
        return rotateClockwiseAround(side.getAxis(), side.getAxisDirection());
    }

    public ExtendedBlockModelRotation rotateClockwiseAround(Direction.Axis axis, Direction.AxisDirection axisDir) {
        var direction = getDirection(RelativeDirection.FRONT);
        var up = getDirection(RelativeDirection.UP);
        Direction newDir;
        Direction newUp;
        if (axisDir == Direction.AxisDirection.POSITIVE) {
            newDir = direction.getClockWise(axis);
            newUp = up.getClockWise(axis);
        } else {
            newDir = direction.getCounterClockWise(axis);
            newUp = up.getCounterClockWise(axis);
        }
        return get(newDir, newUp);
    }
}
