package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.error.SinglePredicateError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PredicateContext {
    private final CurrentBlockInfo blockInfo;
    private final List<PatternError> errors = new ArrayList<>();
    private final Object2IntMap<BasePredicate> globalCache;
    private final @Nullable Object2IntMap<BasePredicate> layerCache;

    public PredicateContext(CurrentBlockInfo blockInfo,
                            Object2IntMap<BasePredicate> globalCache,
                            @Nullable Object2IntMap<BasePredicate> layerCache) {
        this.blockInfo = blockInfo;
        this.globalCache = globalCache;
        this.layerCache = layerCache;
    }

    /// accepts a pattern error
    ///
    /// @return false
    public boolean error(PatternError error) {
        this.errors.add(error);
        return false;
    }

    public List<PatternError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    /// @return the current Level
    public Level level() {
        return Objects.requireNonNull(blockInfo.getLevel());
    }

    /// @return the current Block State
    public BlockState state() {
        return this.blockInfo.retrieveCurrentBlockState();
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
        return this.blockInfo.getPos().immutable();
    }

    public @Nullable BlockEntity blockEntity() {
        return this.blockInfo.retrieveCurrentBlockEntity();
    }

    public static PredicateContext of(CurrentBlockInfo blockInfo,
                                      Object2IntMap<BasePredicate> cache,
                                      @Nullable Object2IntMap<BasePredicate> layerCache) {
        return new PredicateContext(blockInfo, cache, layerCache);
    }

    public Object2IntMap<BasePredicate> globalCache() {
        return globalCache;
    }

    public @Nullable Object2IntMap<BasePredicate> layerCache() {
        return layerCache;
    }

    /// test against global max count
    public boolean testGlobalMax(BasePredicate predicate) {
        if (predicate.skipGlobalTest() || layerCache() == null) return true;
        int count = incrementGlobalCount(predicate);
        if (predicate.testGlobalMax(count)) return true;
        return error(SinglePredicateError.maxCount(predicate, count));
    }

    /// test against slice max count
    public boolean testSliceMax(BasePredicate predicate) {
        if (predicate.skipSliceTest() || layerCache() == null) return true;
        int count = incrementSliceCount(predicate);
        if (predicate.testSliceMax(count)) return true;
        return error(SinglePredicateError.maxLayerCount(predicate, count));
    }

    private int incrementGlobalCount(BasePredicate predicate) {
        globalCache().mergeInt(predicate, 1, Integer::sum);
        return globalCache().getInt(predicate);
    }

    private int incrementSliceCount(BasePredicate predicate) {
        Objects.requireNonNull(layerCache()).mergeInt(predicate, 1, Integer::sum);
        return layerCache().getInt(predicate);
    }

    /// test against global min count
    public boolean testGlobalMin(BasePredicate predicate) {
        if (predicate.skipGlobalTest() || layerCache() == null) return true;
        int count = globalCache().getInt(predicate);
        if (predicate.testGlobalMin(count)) return true;
        return error(SinglePredicateError.minCount(predicate, count));
    }

    /// test against slice min count
    public boolean testSliceMin(BasePredicate predicate) {
        if (predicate.skipSliceTest() || layerCache() == null) return true;
        int count = layerCache().getInt(predicate);
        if (predicate.testSliceMin(count)) return true;
        return error(SinglePredicateError.minLayerCount(predicate, count));
    }
}
