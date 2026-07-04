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
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class PredicateContext {

    @Getter
    private final CurrentBlockInfo blockInfo;
    private final List<PatternError> errors = new ArrayList<>();
    private final Object2IntMap<BasePredicate> globalCache;
    private final @Nullable Object2IntMap<BasePredicate> layerCache;
    @Getter
    @Setter
    private FailureReason failureReason;

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

    public enum FailureReason {
        INTERNAL,
        GLOBAL_MAX,
        GLOBAL_MIN,
        SLICE_MAX,
        SLICE_MIN;
    }
}
