package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.error.BlockMatchingError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;

public class PredicateStates extends BasePredicate {

    public final List<BlockState> states;

    public PredicateStates(BlockState[] states) {
        this(null, states);
    }

    public PredicateStates(@Nullable String debugName, BlockState... states) {
        Validate.noNullElements(states, "Blockstate array has null element at index %s");

        if (states.length == 0) {
            this.states = List.of(Blocks.BARRIER.defaultBlockState());
        } else {
            this.states = List.of(states);
        }
        errorPredicate = state -> {
            return this.states.contains(state.retrieveCurrentBlockState()) ? null :
                    new BlockMatchingError(state.getBlockPos(), this.states.stream()
                            .map(BlockBehaviour.BlockStateBase::getBlock).toList());
        };
        candidates = this.states.stream().map(BlockInfo::fromBlockState).toList();

        if (debugName == null) {
            StringJoiner sb = new StringJoiner("-");
            for (BlockState state : states) {
                sb.add(BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath());
            }
            this.debugName = sb.toString();
        } else {
            this.debugName = debugName;
        }
    }
}
