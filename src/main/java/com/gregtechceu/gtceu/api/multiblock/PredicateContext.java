package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternStringError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.pattern.PatternState;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PredicateContext {

    private final PatternState state;
    @Getter
    protected CurrentBlockInfo currentBlockInfo = new CurrentBlockInfo();
    protected final Object2IntMap<BasePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<BasePredicate> layerCount = new Object2IntOpenHashMap<>();
    @Getter
    @Setter
    private boolean checkLayer = true;

    @Getter
    private boolean checkFlipped = false;

    @Getter
    private FailureReason lastFailureReason = FailureReason.NONE;

    // if (stage == PredicateStage.GLOBAL_MIN || stage == PredicateStage.SLICE_MIN) {
    // this.currentSlice = null;
    // }
    @Setter
    protected PredicateStage stage = PredicateStage.INTERNAL;

    public PredicateContext(PatternState state) {
        this.state = state;
    }

    /// accepts a pattern error
    ///
    /// @return false
    public boolean error(PatternError error) {
        appendError(error);
        return false;
    }

    private void appendError(PatternError error) {
        if (currentSlice != null) {
            getCurrentSliceErrors().add(error);
        } else {
            this.state.setError(error);
        }
        this.lastFailureReason = this.stage.getFailureReason();
        this.checkFlipped = this.lastFailureReason.shouldCheckFlip();
    }

    private List<PatternError> getCurrentSliceErrors() {
        return this.sliceErrors.computeIfAbsent(Objects.requireNonNull(currentSlice), k -> new ArrayList<>());
    }

    /// @return the current Level
    public Level level() {
        return Objects.requireNonNull(currentBlockInfo.getLevel());
    }

    /// @return the current Block State
    public BlockState state() {
        return this.currentBlockInfo.retrieveCurrentBlockState();
    }

    /// @return the current Fluid
    public Fluid fluid() {
        return fluidState().getType();
    }

    /// @return the current Fluid State
    public FluidState fluidState() {
        return state().getFluidState();
    }

    /// @return the current block pos (immutable)
    public BlockPos pos() {
        return this.currentBlockInfo.getPos().immutable();
    }

    public @Nullable BlockEntity blockEntity() {
        return this.currentBlockInfo.retrieveCurrentBlockEntity();
    }

    public Object2IntMap<BasePredicate> globalCache() {
        return this.globalCount;
    }

    /// @return {@code null} if {@link #checkLayer} is false, else returns the layer cache map
    public @Nullable Object2IntMap<BasePredicate> layerCache() {
        return isCheckLayer() ? this.layerCount : null;
    }

    public int incrementGlobalCount(BasePredicate predicate) {
        return globalCache().mergeInt(predicate, 1, Integer::sum);
    }

    public int incrementSliceCount(BasePredicate predicate) {
        return Objects.requireNonNull(layerCache()).mergeInt(predicate, 1, Integer::sum);
    }

    public int getGlobalCount(BasePredicate predicate) {
        return globalCache().getInt(predicate);
    }

    public int getSliceCount(BasePredicate predicate) {
        return Objects.requireNonNull(layerCache()).getInt(predicate);
    }

    public void clearErrors() {
        this.sliceErrors.remove(currentSlice);
        this.lastFailureReason = FailureReason.NONE;
    }

    Object2ObjectMap<Slice, List<PatternError>> sliceErrors = new Object2ObjectAVLTreeMap<>(Slice::compareTo);

    public void commitSliceErrors() {
        for (Slice slice : this.sliceErrors.keySet()) {
            this.state.appendError(PatternStringError.literal("error(s) at %s", slice));
            this.state.appendErrors(this.sliceErrors.get(slice));
        }
        this.sliceErrors.clear();
    }

    private @Nullable Slice currentSlice;

    public void pushSlice(int index, int offset) {
        this.currentSlice = new Slice(index, offset);
    }

    public void updateLevel(Level level) {
        this.currentBlockInfo.setLevel(level);
    }

    public void updatePos(BlockPos pos) {
        this.currentBlockInfo.setCurrentPos(pos);
    }

    public BlockInfo computeBlockInfo() {
        return new BlockInfo(state(), blockEntity());
    }

    public void skipFlipCheck() {
        this.checkFlipped = false;
    }

    public void reset() {
        setStage(PredicateStage.INTERNAL);
        this.currentBlockInfo = new CurrentBlockInfo();
        this.currentSlice = null;
        this.sliceErrors.clear();
        this.globalCount.clear();
        this.layerCount.clear();
    }

    private record Slice(int index, int offset) implements Comparable<Slice> {

        @Override
        public int compareTo(PredicateContext.Slice o) {
            int i = Integer.compare(this.index, o.index);
            if (i == 0) {
                return Integer.compare(this.offset, o.offset);
            }
            return i;
        }
    }

    public enum PredicateStage {

        INTERNAL,
        GLOBAL_MIN,
        GLOBAL_MAX,
        SLICE_MIN,
        SLICE_MAX;

        private FailureReason getFailureReason() {
            return switch (this) {
                case INTERNAL -> FailureReason.INTERNAL;
                case GLOBAL_MIN -> FailureReason.GLOBAL_MIN;
                case GLOBAL_MAX -> FailureReason.GLOBAL_MAX;
                case SLICE_MIN -> FailureReason.SLICE_MIN;
                case SLICE_MAX -> FailureReason.SLICE_MAX;
            };
        }
    }

    public enum FailureReason {

        NONE,
        INTERNAL,
        GLOBAL_MAX,
        GLOBAL_MIN,
        SLICE_MAX,
        SLICE_MIN;

        public boolean shouldCheckFlip() {
            return this == INTERNAL;
        }
    }
}
