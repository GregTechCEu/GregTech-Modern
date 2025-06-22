package com.gregtechceu.gtceu.api.pattern.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

import org.jetbrains.annotations.ApiStatus;

import java.util.Comparator;
import java.util.function.UnaryOperator;

public enum RelativeDirection {

    UP(dir -> dir.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP, Direction.UP),
    DOWN(dir -> dir.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.DOWN, Direction.DOWN),
    LEFT(dir -> {
        if (dir == Direction.UP) return Direction.EAST;
        else if (dir == Direction.DOWN) return Direction.WEST;
        else return dir.getCounterClockWise();
    }, Direction.WEST),
    RIGHT(dir -> {
        if (dir == Direction.UP) return Direction.WEST;
        else if (dir == Direction.DOWN) return Direction.EAST;
        else return dir.getClockWise();
    }, Direction.EAST),
    FRONT(UnaryOperator.identity(), Direction.NORTH),
    BACK(Direction::getOpposite, Direction.SOUTH);

    private final UnaryOperator<Direction> actualDirection;
    /**
     * Equivalent global direction to this relative direction
     * with {@link Direction#NORTH NORTH} as the "forward" direction.
     */
    public final Direction equivalentGlobal;

    RelativeDirection(UnaryOperator<Direction> actualDirection, Direction equivalentGlobal) {
        this.actualDirection = actualDirection;
        this.equivalentGlobal = equivalentGlobal;
    }

    public Direction getActualDirection(Direction direction) {
        return actualDirection.apply(direction);
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
        return getActualDirection(facing).getNormal();
    }

    /**
     * @deprecated Renamed to {@link RelativeDirection#getRelative(Direction, Direction, boolean) getRelative}.
     */
    @ApiStatus.ScheduledForRemoval(inVersion = "8.0.0")
    @Deprecated(since = "7.0.0", forRemoval = true)
    public Direction getRelativeFacing(Direction frontFacing, Direction upwardsFacing, boolean isFlipped) {
        return getRelative(frontFacing, upwardsFacing, isFlipped);
    }

    public Direction getRelative(Direction frontDir, Direction upwardsDir, boolean isFlipped) {
        Direction.Axis frontAxis = frontDir.getAxis();
        return switch (this) {
            case UP -> {
                if (frontAxis == Direction.Axis.Y) {
                    // same direction as upwards facing
                    yield upwardsDir;
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsDir) {
                        case NORTH -> Direction.UP;
                        case SOUTH -> Direction.DOWN;
                        case EAST -> frontDir.getCounterClockWise();
                        default -> frontDir.getClockWise(); // WEST
                    };
                }
            }
            case DOWN -> {
                if (frontAxis == Direction.Axis.Y) {
                    // opposite direction as upwards facing
                    yield upwardsDir.getOpposite();
                } else {
                    // transform the upwards facing into a real facing
                    yield switch (upwardsDir) {
                        case NORTH -> Direction.DOWN;
                        case SOUTH -> Direction.UP;
                        case EAST -> frontDir.getClockWise();
                        default -> frontDir.getCounterClockWise(); // WEST
                    };
                }
            }
            case LEFT -> {
                Direction direction;
                if (frontAxis == Direction.Axis.Y) {
                    direction = frontDir.getStepY() > 0 ? upwardsDir.getClockWise() : upwardsDir.getCounterClockWise();
                } else {
                    direction = switch (upwardsDir) {
                        case NORTH -> frontDir.getCounterClockWise();
                        case SOUTH -> frontDir.getClockWise();
                        case EAST -> Direction.DOWN;
                        default -> Direction.UP; // WEST
                    };
                }
                yield isFlipped ? direction.getOpposite() : direction;
            }
            case RIGHT -> {
                Direction direction;
                if (frontAxis == Direction.Axis.Y) {
                    direction = frontDir.getStepY() > 0 ? upwardsDir.getCounterClockWise() : upwardsDir.getClockWise();
                } else {
                    direction = switch (upwardsDir) {
                        case NORTH -> frontDir.getClockWise();
                        case SOUTH -> frontDir.getCounterClockWise();
                        case EAST -> Direction.UP;
                        default -> Direction.DOWN; // WEST
                    };
                }
                // invert if flipped
                yield isFlipped ? direction.getOpposite() : direction;
            }
            // same direction as front facing, upwards facing doesn't matter
            case FRONT -> frontDir;
            // opposite direction as front facing, upwards facing doesn't matter
            case BACK -> frontDir.getOpposite();
        };
    }

    public Comparator<BlockPos> getSorter(Direction frontDir, Direction upwardsDir, boolean isFlipped) {
        // get the direction to go in for the part sorter
        Direction sorterDirection = getRelative(frontDir, upwardsDir, isFlipped);

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
    public static Direction simulateAxisRotation(Direction newFrontDir, Direction oldFrontDir, Direction upwardsDir) {
        if (newFrontDir == oldFrontDir) return upwardsDir;

        Direction.Axis newAxis = newFrontDir.getAxis();
        Direction.Axis oldAxis = oldFrontDir.getAxis();

        if (newAxis != Direction.Axis.Y && oldAxis != Direction.Axis.Y) {
            // no change needed
            return upwardsDir;
        } else if (newAxis == Direction.Axis.Y && oldAxis != Direction.Axis.Y) {
            // going from horizontal to vertical axis
            Direction newUpwardsDir = switch (upwardsDir) {
                case NORTH -> oldFrontDir.getOpposite();
                case SOUTH -> oldFrontDir;
                case EAST -> oldFrontDir.getCounterClockWise();
                default -> oldFrontDir.getClockWise(); // WEST
            };
            return newFrontDir == Direction.DOWN && upwardsDir.getAxis() == Direction.Axis.Z ?
                    newUpwardsDir.getOpposite() : newUpwardsDir;
        } else if (newAxis != Direction.Axis.Y) {
            // going from vertical to horizontal axis
            Direction newUpwardsDir;
            if (upwardsDir == newFrontDir) {
                newUpwardsDir = Direction.SOUTH;
            } else if (upwardsDir == newFrontDir.getOpposite()) {
                newUpwardsDir = Direction.NORTH;
            } else if (upwardsDir == newFrontDir.getClockWise()) {
                newUpwardsDir = Direction.WEST;
            } else { // getCounterClockWise
                newUpwardsDir = Direction.EAST;
            }
            return oldFrontDir == Direction.DOWN && newUpwardsDir.getAxis() == Direction.Axis.Z ?
                    newUpwardsDir.getOpposite() : newUpwardsDir;
        } else {
            // was on vertical axis and still is. Must have flipped from up to down or vice versa
            return upwardsDir.getOpposite();
        }
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    public static BlockPos offsetPos(BlockPos pos, Direction frontDir, Direction upwardsDir, boolean isFlipped,
                                     int upOffset, int leftOffset, int forwardOffset) {
        if (upOffset == 0 && leftOffset == 0 && forwardOffset == 0) {
            return pos;
        }

        int oX = 0, oY = 0, oZ = 0;
        final Direction relUp = UP.getRelative(frontDir, upwardsDir, isFlipped);
        oX += relUp.getStepX() * upOffset;
        oY += relUp.getStepY() * upOffset;
        oZ += relUp.getStepZ() * upOffset;

        final Direction relLeft = LEFT.getRelative(frontDir, upwardsDir, isFlipped);
        oX += relLeft.getStepX() * leftOffset;
        oY += relLeft.getStepY() * leftOffset;
        oZ += relLeft.getStepZ() * leftOffset;

        final Direction relForward = FRONT.getRelative(frontDir, upwardsDir, isFlipped);
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
     * @param base     the direction to offset
     * @param relative the direction to offset by
     * @param forward  the direction to use as "forward"
     * @return The relative rotation between {@code base} and {@code relative}
     */
    public static RelativeDirection findRelativeOf(Direction base, Direction relative, Direction forward) {
        // Check simple cases first
        if (base == relative) return RelativeDirection.FRONT;
        if (base.getOpposite() == relative) return RelativeDirection.BACK;

        if (base.getAxis().isHorizontal()) { // base is one of N,S,W,E
            if (relative == Direction.UP) return RelativeDirection.UP;
            else if (relative == Direction.DOWN) return RelativeDirection.DOWN;
            else if (relative == base.getCounterClockWise()) return RelativeDirection.LEFT;
            else return RelativeDirection.RIGHT; // getClockWise
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
