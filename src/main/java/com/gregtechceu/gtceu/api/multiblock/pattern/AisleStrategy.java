package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AisleStrategy {

    protected final int[] dimensions = new int[3];
    protected final RelativeDirection[] directions = new RelativeDirection[3];

    @Getter
    @Setter
    private @Nullable BlockPattern pattern;
    private @Nullable BlockPos.MutableBlockPos pos;
    private @Nullable Direction front, up;

    public BlockPos.MutableBlockPos getPos() {
        if (pos == null) throw new IllegalStateException("AisleStrategy.start() has not been called");
        return pos;
    }

    public Direction getFront() {
        if (front == null) throw new IllegalStateException("AisleStrategy.start() has not been called");
        return front;
    }

    public Direction getUp() {
        if (up == null) throw new IllegalStateException("AisleStrategy.start() has not been called");
        return up;
    }

    /**
     * Checks the aisles
     *
     * @param flip Whether this is a flipped pattern check.
     * @return Whether the pattern is formed after this.
     */
    public abstract boolean check(PatternState state, boolean flip);

    /**
     * Gets the order in which aisles should be displayed (recipe viewers) or built (terminal structure builder).
     *
     * @param tag The tag information, the same one that is passed through
     *            {@link IBlockPattern#autobuild}
     * @return Array where the i-th element specifies that at offset i there would be aisle a_i
     */
    public abstract int[] getDefaultAisles(CompoundTag tag);

    /**
     * Called at the start of a structure check.
     */
    protected void start(BlockPos.MutableBlockPos pos, Direction front, Direction up) {
        this.pos = pos;
        this.front = front;
        this.up = up;
    }

    /**
     * No more aisles will be added. Check preconditions and throw exceptions here.
     */
    protected void finish(int[] dimensions, RelativeDirection[] directions, List<PatternAisle> aisles) {
        System.arraycopy(dimensions, 0, this.dimensions, 0, 3);
        System.arraycopy(directions, 0, this.directions, 0, 3);
    }

    protected boolean checkAisle(PatternState state, int index, int offset, boolean flip) {
        if (pattern == null) throw new IllegalStateException("BlockPattern not set.");
        return pattern.checkAisle(getPos(), state, getFront(), getUp(), index, offset, flip);
    }
}
