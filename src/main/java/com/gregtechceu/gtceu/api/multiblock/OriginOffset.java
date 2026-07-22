package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class OriginOffset {

    private int x, y, z;

    public static final OriginOffset ZERO = new OriginOffset();

    public OriginOffset() {
        this(0, 0, 0);
    }

    public OriginOffset(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static OriginOffset of(int xi, int yi, int zi) {
        return new OriginOffset(xi, yi, zi);
    }

    public static OriginOffset of(RelativeDirection direction, int amount) {
        return new OriginOffset().move(direction, amount);
    }

    public OriginOffset move(int xi, int yi, int zi) {
        return move(RelativeDirection.LEFT, RelativeDirection.UP, RelativeDirection.FRONT, xi, yi, zi);
    }

    public OriginOffset move(RelativeDirection x, RelativeDirection y, RelativeDirection z, int xi, int yi, int zi) {
        RelativeDirection.validateFacingsArray(new RelativeDirection[] { x, y, z });
        return move(x, xi).move(y, yi).move(z, zi);
    }

    public OriginOffset move(RelativeDirection dir, int amount) {
        amount *= dir.ordinal() % 2 == 0 ? 1 : -1;
        switch (dir.ordinal()) {
            case 0, 1 -> y += amount;
            case 2, 3 -> x += amount;
            case 4, 5 -> z += amount;
            default -> throw new IllegalStateException("Unexpected value: " + dir.ordinal());
        }

        return this;
    }

    public OriginOffset move(RelativeDirection dir) {
        return move(dir, 1);
    }

    public int get(RelativeDirection dir) {
        return switch (dir.ordinal()) {
            case 0, 1 -> y * (dir.ordinal() == 0 ? 1 : -1);
            case 2, 3 -> x * (dir.ordinal() == 2 ? 1 : -1);
            case 4, 5 -> z * (dir.ordinal() == 4 ? 1 : -1);
            default -> throw new IllegalStateException("Unexpected value: " + dir.ordinal());
        };
    }

    /**
     * Applies this offset to {@code pos} based on the front, up and flip values
     * 
     * @param pos   BlockPos to modify
     * @param front front facing direction
     * @param up    upwards facing direction
     * @param flip  whether to flip orientation
     */
    public void apply(BlockPos.MutableBlockPos pos, Direction front, Direction up, boolean flip) {
        pos.move(RelativeDirection.LEFT.getRelativeFacing(front, up, flip), x);
        pos.move(RelativeDirection.UP.getRelativeFacing(front, up, flip), y);
        pos.move(RelativeDirection.FRONT.getRelativeFacing(front, up, flip), z);
    }

    /**
     *
     * @return BlockPos from values
     */
    public BlockPos toBlockPos() {
        return new BlockPos(x, y, z);
    }
}
