package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PredicateContext {

    @Getter
    protected List<PatternError> errors = new ArrayList<>();
    @Getter
    protected CurrentBlockInfo currentBlockInfo = new CurrentBlockInfo();
    protected final Object2IntMap<BasePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<BasePredicate> layerCount = new Object2IntOpenHashMap<>();
    @Getter
    @Setter
    private boolean checkLayer = true;

    @Getter
    protected FailureReason lastFailureReason = FailureReason.NONE;

    @Setter
    protected PredicateStage stage = PredicateStage.INTERNAL;

    /// accepts a pattern error
    ///
    /// @return false
    public boolean error(PatternError error) {
        appendError(error);
        return false;
    }

    private void appendError(PatternError error) {
        this.errors.add(error);
        this.lastFailureReason = this.stage.getFailureReason();
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
        this.errors = new ArrayList<>();
        this.lastFailureReason = FailureReason.NONE;
    }

    Object2ObjectMap<Slice, List<PatternError>> sliceErrors = new Object2ObjectArrayMap<>();

    public void commitSliceErrors() {
        for (Slice slice : this.sliceErrors.keySet()) {
            this.errors.addAll(this.sliceErrors.get(slice));
        }
        this.sliceErrors.clear();
    }

    public void pushSliceErrors(int index, int offset) {
        if (this.errors.isEmpty()) return;
        Slice slice = new Slice(index, offset);
        this.sliceErrors.merge(slice, this.errors, (l1, l2) -> {
            l1.addAll(l2);
            return l1;
        });
        clearErrors();
    }

    public void updateLevel(Level level) {
        this.currentBlockInfo.setLevel(level);
    }

    private record Slice(int index, int offset) {}

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
