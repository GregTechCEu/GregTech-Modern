package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
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

import it.unimi.dsi.fastutil.ints.Int2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.objects.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PredicateContext {

    @Nullable
    private final PatternState state;
    @Getter
    protected CurrentBlockInfo currentBlockInfo = new CurrentBlockInfo();
    protected final Object2IntMap<BasePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<BasePredicate> layerCount = new Object2IntOpenHashMap<>();

    private final Int2ObjectMap<List<PatternError>> sliceErrors = new Int2ObjectAVLTreeMap<>(
            IntComparators.NATURAL_COMPARATOR);

    private int currentSlice = 0;
    @Getter
    @Setter
    private boolean checkLayer = true;

    @Getter
    private boolean checkFlipped = true;

    @Getter
    private FailureReason lastFailureReason = FailureReason.NONE;

    @Setter
    protected PredicateStage stage = PredicateStage.INTERNAL;

    public PredicateContext(@Nullable PatternState state) {
        this.state = state;
    }

    /// If a multiblock is currently being iterated, errors are queued to be added to the
    /// PatternState later
    ///
    /// @param error The error to add to PatternState
    public void appendError(PatternError error) {
        if (currentSlice != -1) {
            getCurrentSliceErrors().add(error);
            this.lastFailureReason = this.stage.getFailureReason();
        }
    }

    public void clearErrors() {
        this.sliceErrors.remove(currentSlice);
        this.lastFailureReason = FailureReason.NONE;
    }

    private List<PatternError> getCurrentSliceErrors() {
        return this.sliceErrors.computeIfAbsent(currentSlice, k -> new ArrayList<>());
    }

    public void pushSlice() {
        this.currentSlice++;
    }

    public void popSlice() {
        clearErrors();
        this.currentSlice--;
    }

    public void commitSliceErrors() {
        if (state == null) return;
        for (var entry : this.sliceErrors.int2ObjectEntrySet()) {
            this.state.appendErrors(entry.getValue());
        }
        if (this.checkFlipped) {
            this.checkFlipped = this.lastFailureReason.shouldCheckFlip();
        }
        this.sliceErrors.clear();
        this.currentSlice = -1;
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

    public void clearGlobalCounts() {
        this.globalCount.clear();
    }

    public void clearLayerCounts() {
        this.layerCount.clear();
    }

    public int incrementGlobalCount(BasePredicate predicate) {
        return this.globalCount.mergeInt(predicate, 1, Integer::sum);
    }

    public int incrementSliceCount(BasePredicate predicate) {
        if (!checkLayer) return 0;
        return this.layerCount.mergeInt(predicate, 1, Integer::sum);
    }

    public int getGlobalCount(BasePredicate predicate) {
        return this.globalCount.getInt(predicate);
    }

    public int getSliceCount(BasePredicate predicate) {
        if (!checkLayer) return 0;
        return this.layerCount.getInt(predicate);
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

    public void reset() {
        setStage(PredicateStage.INTERNAL);
        this.checkFlipped = true;
        this.currentBlockInfo = new CurrentBlockInfo();
        this.currentSlice = 0;
        this.sliceErrors.clear();
        this.clearGlobalCounts();
        this.clearLayerCounts();
    }

    public void skipFlipCheck() {
        this.checkFlipped = false;
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
