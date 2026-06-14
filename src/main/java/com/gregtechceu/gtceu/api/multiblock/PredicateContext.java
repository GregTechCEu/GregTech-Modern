package com.gregtechceu.gtceu.api.multiblock;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.pattern.CurrentBlockInfo;
import com.gregtechceu.gtceu.api.multiblock.predicates.BasePredicate;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public record PredicateContext(CurrentBlockInfo blockInfo,
                               Consumer<PatternError> errorConsumer,
                               Object2IntMap<BasePredicate> globalCache,
                               @Nullable Object2IntMap<BasePredicate> layerCache) {

    ///  accepts a pattern error
    /// @return false
    public boolean error(PatternError error) {
        this.errorConsumer.accept(error);
        return false;
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

    public static PredicateContext of(CurrentBlockInfo blockInfo, Consumer<PatternError> errorConsumer,
                                      Object2IntMap<BasePredicate> cache,
                                      @Nullable Object2IntMap<BasePredicate> layerCache) {
        return new PredicateContext(blockInfo, errorConsumer, cache, layerCache);
    }
}
