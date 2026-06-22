package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SliceStrategy {

    protected final int[] dimensions = new int[3];
    protected final RelativeDirection[] directions = new RelativeDirection[3];

    @Getter
    @Setter
    private @Nullable BlockPattern pattern;
    private @Nullable BlockPos.MutableBlockPos pos;
    private @Nullable Direction front, up;

    public BlockPos.MutableBlockPos getPos() {
        if (pos == null) throw new IllegalStateException("SliceStrategy.start() has not been called");
        return pos;
    }

    public Direction getFront() {
        if (front == null) throw new IllegalStateException("SliceStrategy.start() has not been called");
        return front;
    }

    public Direction getUp() {
        if (up == null) throw new IllegalStateException("SliceStrategy.start() has not been called");
        return up;
    }

    /**
     * Checks the slices
     *
     * @param flip Whether this is a flipped pattern check.
     * @return Whether the pattern is formed after this.
     */
    public abstract boolean check(PatternState state, boolean flip);

    /**
     * Called at the start of a structure check.
     */
    protected void start(BlockPos.MutableBlockPos pos, Direction front, Direction up) {
        this.pos = pos;
        this.front = front;
        this.up = up;
    }

    /**
     * No more slices will be added. Check preconditions and throw exceptions here.
     */
    protected void finish(int[] dimensions, RelativeDirection[] directions, List<PatternSlice> slices) {
        System.arraycopy(dimensions, 0, this.dimensions, 0, 3);
        System.arraycopy(directions, 0, this.directions, 0, 3);
    }

    protected boolean checkSlice(PatternState state, int index, int offset, boolean flip) {
        if (pattern == null) throw new IllegalStateException("BlockPattern not set.");
        return pattern.checkSlice(getPos(), state, getFront(), getUp(), index, offset, flip);
    }
}
