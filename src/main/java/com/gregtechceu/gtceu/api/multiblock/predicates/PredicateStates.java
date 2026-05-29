package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PredicateStates extends BasePredicate {

    public final BlockState[] states;

    public PredicateStates(BlockState[] states) {
        this(null, states);
    }

    public PredicateStates(@Nullable String debugName, BlockState... states) {
        Validate.noNullElements(states, "Blockstate array has null element at index %s");

        if (states.length == 0) this.states = new BlockState[] { Blocks.BARRIER.defaultBlockState() };
        else this.states = Arrays.stream(states).toArray(BlockState[]::new);
        errorPredicate = state -> ArrayUtils.contains(this.states, state.getBlockState()) ?
                null : PatternError.PLACEHOLDER;
        candidates = Arrays.stream(this.states).map(BlockInfo::fromBlockState).toList();

        if (debugName == null) {
            /*
             * StringJoiner sb = new StringJoiner("-");
             * for(BlockState bs : states) {
             * sb.add(bs.toString());
             * }
             */
            // this.debugName = "ERm why are you using block states";
        } else {
            this.debugName = debugName;
        }
    }
}
