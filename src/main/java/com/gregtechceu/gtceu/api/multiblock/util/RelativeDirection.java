package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.BinaryOperator;
import java.util.function.ToIntFunction;

/**
 * Relative direction when facing horizontally and vertically
 */
public enum RelativeDirection implements StringRepresentable {

    UP((f, u) -> u),
    DOWN((f, u) -> u.getOpposite()),
    LEFT((f, u) -> GTUtil.cross(f, u).getOpposite()),
    RIGHT(GTUtil::cross),
    FRONT((f, u) -> f),
    BACK((f, u) -> f.getOpposite());

    private final BinaryOperator<Direction> facingFunction;

    public static final EnumCodec<RelativeDirection> CODEC = StringRepresentable
            .fromEnum(RelativeDirection::values);

    public static final RelativeDirection[] VALUES = values();

    RelativeDirection(BinaryOperator<Direction> facingFunction) {
        this.facingFunction = facingFunction;
    }

    public RelativeDirection getOpposite() {
        return VALUES[oppositeOrdinal()];
    }

    public int oppositeOrdinal() {
        return switch (ordinal()) {
            case 0 -> 1;
            case 1 -> 0;
            case 2 -> 3;
            case 3 -> 2;
            case 4 -> 5;
            case 5 -> 4;
            default -> throw new IllegalStateException("Unexpected value: " + ordinal());
        };
    }

    public Direction getRelativeFacing(Direction frontFacing, Direction upwardsFacing) {
        if (frontFacing.getAxis() == upwardsFacing.getAxis()) {
            return Direction.UP;
            // throw new IllegalArgumentException("front facing and up facing must be on different axes");
        }
        return facingFunction.apply(frontFacing, upwardsFacing);
    }

    public Direction getRelativeFacing(Direction frontFacing, Direction upwardsFacing, boolean isFlipped) {
        return (isFlipped && (this == LEFT || this == RIGHT)) ?
                getRelativeFacing(frontFacing, upwardsFacing).getOpposite() :
                getRelativeFacing(frontFacing, upwardsFacing);
    }

    public ToIntFunction<BlockPos> getPosSorter(Direction frontFacing, Direction upwardsFacing, boolean isFlipped) {
        // get the direction to go in for the part sorter
        Direction sorterDirection = getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

        // Determined by Direction.Axis + Direction.AxisDirection
        return switch (sorterDirection) {
            case UP -> BlockPos::getY;
            case DOWN -> pos -> -pos.getY();
            case EAST -> BlockPos::getX;
            case WEST -> pos -> -pos.getX();
            case NORTH -> pos -> -pos.getZ();
            case SOUTH -> BlockPos::getZ;
        };
    }

    public ToIntFunction<IMultiPart> getMultiSorter(Direction frontFacing, Direction upwardsFacing, boolean isFlipped) {
        // get the direction to go in for the part sorter
        Direction sorterDirection = getRelativeFacing(frontFacing, upwardsFacing, isFlipped);

        // Determined by Direction.Axis + Direction.AxisDirection
        return switch (sorterDirection) {
            case UP -> p -> p.self().getBlockPos().getY();
            case DOWN -> p -> -p.self().getBlockPos().getY();
            case EAST -> p -> p.self().getBlockPos().getX();
            case WEST -> p -> -p.self().getBlockPos().getX();
            case NORTH -> p -> -p.self().getBlockPos().getZ();
            case SOUTH -> p -> p.self().getBlockPos().getZ();
        };
    }

    /**
     * Simulates rotating the controller around an axis to get to a new front facing.
     *
     * @return Returns the new upwards facing.
     */
    public static Direction simulateAxisRotation(Direction newFrontFacing, Direction oldFrontFacing,
                                                 Direction upwardsFacing) {
        if (newFrontFacing.getAxis() == oldFrontFacing.getAxis()) return upwardsFacing;

        Direction cross = GTUtil.cross(newFrontFacing, oldFrontFacing);

        assert cross != null;
        if (cross.getAxis() == upwardsFacing.getAxis()) return upwardsFacing;

        if (oldFrontFacing.getClockWise(cross.getAxis()) == newFrontFacing)
            return oldFrontFacing.getOpposite();

        return oldFrontFacing;
    }

    /**
     * Offset a BlockPos relatively in any direction by any amount. Pass negative values to offset down, right or
     * backwards.
     */
    public static BlockPos offsetPos(BlockPos pos, Direction frontFacing, Direction upwardsFacing, boolean isFlipped,
                                     int upOffset, int leftOffset, int forwardOffset) {
        BlockPos.MutableBlockPos mbp = pos.mutable();
        mbp.move(UP.getRelativeFacing(frontFacing, upwardsFacing, isFlipped), upOffset);
        mbp.move(LEFT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped), leftOffset);
        mbp.move(FRONT.getRelativeFacing(frontFacing, upwardsFacing, isFlipped), forwardOffset);
        return mbp.immutable();
    }

    public static <T extends Enum<T>> void validateFacingsArray(T[] facings) {
        if (facings.length != 3) throw new IllegalArgumentException("Facings must be array of length 3!");

        int c = 0;
        for (int i = 0; i < 3; i++) {
            c |= (1 << facings[i].ordinal() / 2);
        }

        if (c != 7) throw new IllegalArgumentException("The 3 facings must use each axis exactly once!");
    }

    /**
     * @param other The other direction to check
     * @return Whether both directions are on the same axis
     */
    public boolean isSameAxis(RelativeDirection other) {
        return (other.ordinal() ^ this.ordinal() & 0x01) != 0;
    }

    public static Direction getActualDirection(Direction original, Direction current, Direction direction) {
        return findRelativeOf(original, current).applyDirection(direction);
    }

    /**
     * Finds the difference of {@code baseDir} and {@code relativeDir} as a relative direction.
     * <br>
     * If {@code baseDir} is vertical (e.g. {@link Direction#UP UP} or {@link Direction#DOWN DOWN}),
     * the rotation is calculated with {@link Direction#NORTH NORTH} as the upwards direction.
     *
     * @param baseDir     the direction to offset
     * @param relativeDir the direction to offset by
     * @return The difference of {@code baseDir} and {@code relativeDir} as a relative direction
     */
    public static RelativeDirection findRelativeOf(Direction baseDir, Direction relativeDir) {
        return findRelativeOf(baseDir, relativeDir, Direction.UP);
    }

    /**
     * Finds the difference of {@code baseDir} and {@code relativeDir} as a relative direction.
     * <br>
     * If {@code baseDir} is vertical, the rotation is calculated with {@code upwardsDir} as the "upwards" direction.
     *
     * @param baseDir     the direction to offset
     * @param relativeDir the direction to offset by
     * @param upwardsDir  the upwards direction
     * @return The difference of {@code baseDir} and {@code relativeDir} as a relative direction
     */
    public static RelativeDirection findRelativeOf(Direction baseDir, Direction relativeDir, Direction upwardsDir) {
        // Check simple cases first
        if (baseDir == relativeDir) return RelativeDirection.FRONT;
        if (baseDir.getOpposite() == relativeDir) return RelativeDirection.BACK;

        if (baseDir.getAxis().isHorizontal()) { // baseDir is one of N,S,W,E
            if (relativeDir == Direction.UP) return RelativeDirection.UP;
            else if (relativeDir == Direction.DOWN) return RelativeDirection.DOWN;
            else if (relativeDir == baseDir.getCounterClockWise()) return RelativeDirection.LEFT;
            else return RelativeDirection.RIGHT; // getClockWise
        } else { // baseDir is UP or DOWN
            if (upwardsDir.getAxis() == Direction.Axis.Y) {
                throw new IllegalStateException("upwardsDir must be a horizontal direction! is " + upwardsDir);
            }

            if (relativeDir == upwardsDir.getCounterClockWise()) {
                return RelativeDirection.LEFT;
            } else if (relativeDir == upwardsDir.getClockWise()) {
                return RelativeDirection.RIGHT;
            } else { // relativeDir is NORTH or SOUTH (assuming upwardsDir is NORTH)
                RelativeDirection dir;
                if (relativeDir == upwardsDir.getOpposite()) dir = RelativeDirection.UP;
                else dir = RelativeDirection.DOWN; // relativeDir == NORTH

                if (baseDir == Direction.DOWN) dir = dir.getOpposite();
                return dir;
            }
        }
    }

    public Direction getDefaultFacing() {
        return switch (this) {
            case UP -> Direction.UP;
            case DOWN -> Direction.DOWN;
            case LEFT -> Direction.WEST;
            case RIGHT -> Direction.EAST;
            case FRONT -> Direction.NORTH;
            case BACK -> Direction.SOUTH;
        };
    }

    public Direction applyDirection(Direction dir) {
        return switch (this) {
            case UP -> dir.getAxis() == Direction.Axis.Y ? Direction.NORTH : Direction.UP;
            case DOWN -> dir.getAxis() == Direction.Axis.Y ? Direction.SOUTH : Direction.DOWN;
            case LEFT -> dir.getAxis().isHorizontal() ? dir.getCounterClockWise() :
                    dir.getAxisDirection().getStep() > 0 ? Direction.WEST : Direction.EAST;
            case RIGHT -> dir.getAxis().isHorizontal() ? dir.getClockWise() :
                    dir.getAxisDirection().getStep() > 0 ? Direction.EAST : Direction.WEST;
            case FRONT -> dir;
            case BACK -> dir.getOpposite();
        };
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
