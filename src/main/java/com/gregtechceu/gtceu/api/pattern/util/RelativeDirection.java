package com.gregtechceu.gtceu.api.pattern.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import java.util.function.Function;
import java.util.function.ToIntFunction;

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
        return getRelativeFacing(facing, Direction.NORTH, false);
    }

    public boolean isSameAxis(RelativeDirection dir) {
        return this.axis == dir.axis;
    }

    public RelativeDirection getOpposite() {
        return switch (this) {
            case UP -> DOWN;
            case DOWN -> UP;
            case LEFT -> RIGHT;
            case RIGHT -> LEFT;
            case FRONT -> BACK;
            case BACK -> FRONT;
        };
    }

    public Vec3i applyVec3i(Direction facing) {
        return getActualFacing(facing).getNormal();
    }

    public Direction getRelativeFacing(Direction frontFacing, Direction upwardsFacing, boolean isFlipped) {
        Direction.Axis frontAxis = frontFacing.getAxis();
        return switch (this) {
            case UP -> {
                if (frontAxis == Direction.Axis.Y) {
                    // same direction as upwards facing
                    yield upwardsFacing;
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsFacing) {
                        case NORTH -> Direction.UP;
                        case SOUTH -> Direction.DOWN;
                        case EAST -> frontFacing.getCounterClockWise();
                        default -> frontFacing.getClockWise(); // WEST
                    };
                }
            }
            case DOWN -> {
                if (frontAxis == Direction.Axis.Y) {
                    // opposite direction as upwards facing
                    yield upwardsFacing.getOpposite();
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsFacing) {
                        case NORTH -> Direction.DOWN;
                        case SOUTH -> Direction.UP;
                        case EAST -> frontFacing.getClockWise();
                        default -> frontFacing.getCounterClockWise(); // WEST
                    };
                }
            }
            case LEFT -> {
                Direction facing;
                if (frontAxis == Direction.Axis.Y) {
                    facing = frontFacing.getStepY() > 0 ? upwardsFacing.getClockWise() :
                            upwardsFacing.getCounterClockWise();
                } else {
                    facing = switch (upwardsFacing) {
                        case NORTH -> frontFacing.getCounterClockWise();
                        case SOUTH -> frontFacing.getClockWise();
                        case EAST -> Direction.DOWN;
                        default -> Direction.UP; // WEST
                    };
                }
                yield isFlipped ? facing.getOpposite() : facing;
            }
            case RIGHT -> {
                Direction facing;
                if (frontAxis == Direction.Axis.Y) {
                    facing = frontFacing.getStepY() > 0 ? upwardsFacing.getCounterClockWise() :
                            upwardsFacing.getClockWise();
                } else {
                    facing = switch (upwardsFacing) {
                        case NORTH -> frontFacing.getClockWise();
                        case SOUTH -> frontFacing.getCounterClockWise();
                        case EAST -> Direction.UP;
                        default -> Direction.DOWN; // WEST
                    };
                }
                // invert if flipped
                yield isFlipped ? facing.getOpposite() : facing;
            }
            // same direction as front facing, upwards facing doesn't matter
            case FRONT -> frontFacing;
            // opposite direction as front facing, upwards facing doesn't matter
            case BACK -> frontFacing.getOpposite();
        };
    }

    public Comparator<BlockPos> getSorter(Direction frontFacing, Direction upwardsFacing, boolean isFlipped) {
        // get the direction to go in for the part sorter
        Direction sorterDirection = getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

        // Determined by Direction.Axis + Direction.AxisDirection
        return switch (sorterDirection) {
            case UP -> Comparator.comparingInt(BlockPos::getY);
            case DOWN -> Comparator.comparingInt(pos -> -pos.getY());
            case EAST -> Comparator.comparingInt(BlockPos::getX);
            case WEST -> Comparator.comparingInt(pos -> -pos.getX());
            case NORTH -> Comparator.comparingInt(BlockPos::getZ);
            case SOUTH -> Comparator.comparingInt(pos -> -pos.getZ());
        };
    }

    /**
     * Simulates rotating the controller around an axis to get to a new front facing.
     *
     * @return Returns the new upwards facing.
     */
    public static Direction simulateAxisRotation(Direction newFrontFacing, Direction oldFrontFacing,
                                                 Direction upwardsFacing) {
        if (newFrontFacing == oldFrontFacing) return upwardsFacing;

        Direction.Axis newAxis = newFrontFacing.getAxis();
        Direction.Axis oldAxis = oldFrontFacing.getAxis();

        if (newAxis != Direction.Axis.Y && oldAxis != Direction.Axis.Y) {
            // no change needed
            return upwardsFacing;
        } else if (newAxis == Direction.Axis.Y && oldAxis != Direction.Axis.Y) {
            // going from horizontal to vertical axis
            Direction newUpwardsFacing = switch (upwardsFacing) {
                case NORTH -> oldFrontFacing.getOpposite();
                case SOUTH -> oldFrontFacing;
                case EAST -> oldFrontFacing.getCounterClockWise();
                default -> oldFrontFacing.getClockWise(); // WEST
            };
            return newFrontFacing == Direction.DOWN && upwardsFacing.getAxis() == Direction.Axis.Z ?
                    newUpwardsFacing.getOpposite() : newUpwardsFacing;
        } else if (newAxis != Direction.Axis.Y) {
            // going from vertical to horizontal axis
            Direction newUpwardsFacing;
            if (upwardsFacing == newFrontFacing.getOpposite()) {
                newUpwardsFacing = Direction.NORTH;
            } else if (upwardsFacing == newFrontFacing) {
                newUpwardsFacing = Direction.SOUTH;
            } else if (upwardsFacing == newFrontFacing.getClockWise()) {
                newUpwardsFacing = Direction.WEST;
            } else { // getCounterClockWise
                newUpwardsFacing = Direction.EAST;
            }
            return oldFrontFacing == Direction.DOWN && newUpwardsFacing.getAxis() == Direction.Axis.Z ?
                    newUpwardsFacing.getOpposite() : newUpwardsFacing;
        } else {
            // was on vertical axis and still is. Must have flipped from up to down or vice versa
            return upwardsFacing.getOpposite();
        }
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    public static BlockPos offsetPos(BlockPos pos, Direction frontFacing, Direction upwardsFacing, boolean isFlipped,
                                     int upOffset, int leftOffset, int forwardOffset) {
        if (upOffset == 0 && leftOffset == 0 && forwardOffset == 0) {
            return pos;
        }

        int oX = 0, oY = 0, oZ = 0;
        final Direction relUp = UP.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relUp.getStepX() * upOffset;
        oY += relUp.getStepY() * upOffset;
        oZ += relUp.getStepZ() * upOffset;

        final Direction relLeft = LEFT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relLeft.getStepX() * leftOffset;
        oY += relLeft.getStepY() * leftOffset;
        oZ += relLeft.getStepZ() * leftOffset;

        final Direction relForward = FRONT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped);
        oX += relForward.getStepX() * forwardOffset;
        oY += relForward.getStepY() * forwardOffset;
        oZ += relForward.getStepZ() * forwardOffset;

        return pos.offset(oX, oY, oZ);
    }

    public static Direction getActualDirection(Direction original, Direction current, Direction direction) {
        return findRelativeOf(original, current).getActualDirection(direction);
    }

    /**
     * Finds the relative rotation between {@code base} and {@code relative}.
     * <br>
     * If {@code base} is vertical (e.g. {@link Direction#UP UP} or {@link Direction#DOWN DOWN}),
     * the rotation is calculated with {@link Direction#NORTH NORTH} as the "forward" direction.
     * 
     * @param base     the direction to offset
     * @param relative the direction to offset by
     * @return The relative rotation between {@code base} and {@code relative}
     */
    public static RelativeDirection findRelativeOf(Direction base, Direction relative) {
        return findRelativeOf(base, relative, Direction.NORTH);
    }

    /**
     * Finds the relative rotation between {@code base} and {@code relative}.
     * <br>
     * If {@code base} is vertical (e.g. {@link Direction#UP UP} or {@link Direction#DOWN DOWN}),
     * the rotation is calculated with {@code forward} as the "forward" direction.
     * 
     * @param base     the direction to relative
     * @param relative the direction to relative by
     * @param forward  the direction to use as "forward"
     */
    public static RelativeDirection findRelativeOf(Direction base, Direction relative, Direction forward) {
        // Check simple cases first
        if (base == relative) return RelativeDirection.FRONT;
        if (base.getOpposite() == relative) return RelativeDirection.BACK;

        if (base.getAxis().isHorizontal()) { // base is one of N,S,W,E
            if (relative == Direction.UP) return RelativeDirection.UP;
            else if (relative == Direction.DOWN) return RelativeDirection.DOWN;
            else if (base.getCounterClockWise() == relative) return RelativeDirection.LEFT;
            else return RelativeDirection.RIGHT; // base.getClockWise() == relative
        } else { // base is UP or DOWN
            if (forward.getAxis() == Direction.Axis.Y) {
                throw new IllegalStateException("forward must be a horizontal direction! is %s".formatted(forward));
            }
            Direction globalLeft = forward.getCounterClockWise();
            Direction globalRight = forward.getClockWise();
            Direction globalBack = forward.getOpposite();

            if (relative == globalLeft) {
                return RelativeDirection.LEFT;
            } else if (relative == globalRight) {
                return RelativeDirection.RIGHT;
            } else { // relative is NORTH or SOUTH (assuming forward is NORTH)
                RelativeDirection dir;
                if (relative == globalBack) dir = RelativeDirection.UP;
                else dir = RelativeDirection.DOWN; // relative == NORTH

                if (base == Direction.DOWN) dir = dir.getOpposite();
                return dir;
            }
        }
    }
}
