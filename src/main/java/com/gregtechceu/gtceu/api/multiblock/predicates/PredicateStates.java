package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.error.PatternError;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;

public class PredicateStates extends BasePredicate {

    public final BlockState[] states;

    public PredicateStates(BlockState[] states) {
        this(null, states);
    }

    public PredicateStates(String debugName, BlockState... states) {
        if (states.length == 0) this.states = new BlockState[] { Blocks.BARRIER.defaultBlockState() };
        else this.states = Arrays.stream(states).filter(Objects::nonNull).toArray(BlockState[]::new);
        errorPredicate = state -> ArrayUtils.contains(this.states, state.getBlockState()) ?
                null : PatternError.PLACEHOLDER;
        candidates = (tag) -> Arrays.stream(this.states).map(BlockInfo::fromBlockState).toArray(BlockInfo[]::new);

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
