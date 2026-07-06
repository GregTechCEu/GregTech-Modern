package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.error.PartAbilityError;
import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PredicateContext {

    protected final List<PatternError> errors = new ArrayList<>();
    protected final List<PatternError> commitedErrors = new ArrayList<>();
    protected final Object2ObjectMap<BasePredicate, BitSet> validationMap = new Object2ObjectArrayMap<>();
    @Getter
    protected CurrentBlockInfo currentBlockInfo = new CurrentBlockInfo();
    protected final Object2IntMap<BasePredicate> globalCount = new Object2IntOpenHashMap<>();
    protected final Object2IntMap<BasePredicate> layerCount = new Object2IntOpenHashMap<>();
    @Getter
    private FailureReason lastFailureReason;

    /// accepts a pattern error
    ///
    /// @return false
    private boolean error(PatternError error) {
        this.errors.add(error);
        return false;
    }

    public boolean abilityError(PartAbility ability) {
        return internalError(new PartAbilityError(pos(), ability));
    }

    public boolean blockMatchingError(List<Block> blocks) {
        return internalError(new BlockMatchingError(pos(), blocks));
    }

    public boolean internalError(PatternError error) {
        this.lastFailureReason = FailureReason.INTERNAL;
        return error(error);
    }

    public boolean globalError(BasePredicate predicate, boolean min) {
        this.lastFailureReason = min ? FailureReason.GLOBAL_MIN : FailureReason.GLOBAL_MAX;
        SinglePredicateError error = min
                ? SinglePredicateError.minCount(predicate, getGlobalCount(predicate))
                : SinglePredicateError.maxCount(predicate, getGlobalCount(predicate));
        return error(error);
    }

    public boolean sliceError(BasePredicate predicate, boolean min) {
        this.lastFailureReason = min ? FailureReason.SLICE_MIN : FailureReason.SLICE_MAX;
        SinglePredicateError error = min
                ? SinglePredicateError.minLayerCount(predicate, getSliceCount(predicate))
                : SinglePredicateError.maxLayerCount(predicate, getSliceCount(predicate));
        return error(error);
    }

    public void commitErrors() {
        this.commitedErrors.addAll(this.errors);
        this.errors.clear();
    }

    public PredicateContext appendError(PatternError error) {
        this.errors.add(error);
        return this;
    }

    public PredicateContext setFailureReason(FailureReason reason) {
        this.lastFailureReason = reason;
        return this;
    }

    public List<PatternError> getErrors() {
        return Collections.unmodifiableList(this.commitedErrors);
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

    public @Nullable Object2IntMap<BasePredicate> layerCache() {
        return this.layerCount;
    }

    public int incrementGlobalCount(BasePredicate predicate) {
        globalCache().mergeInt(predicate, 1, Integer::sum);
        return globalCache().getInt(predicate);
    }

    public int incrementSliceCount(BasePredicate predicate) {
        Objects.requireNonNull(layerCache()).mergeInt(predicate, 1, Integer::sum);
        return layerCache().getInt(predicate);
    }

    public int getGlobalCount(BasePredicate predicate) {
        return globalCache().getInt(predicate);
    }

    public int getSliceCount(BasePredicate predicate) {
        return Objects.requireNonNull(layerCache()).getInt(predicate);
    }

    public void updateState(BasePredicate predicate, FailureReason index, boolean passed) {
        validationMap.computeIfAbsent(predicate, p -> new BitSet())
                .set(index.ordinal(), passed);
    }

    public enum FailureReason {
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
