package com.gregtechceu.gtceu.api.pattern.predicates;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;

public class PredicateStates extends SimplePredicate {

    public BlockState[] states = new BlockState[0];

    public PredicateStates() {
        super("states");
    }

    public PredicateStates(BlockState... states) {
        this();
        this.states = states;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        states = Arrays.stream(states).filter(Objects::nonNull).toArray(BlockState[]::new);
        if (states.length == 0) states = new BlockState[] { Blocks.BARRIER.defaultBlockState() };
        predicate = state -> ArrayUtils.contains(states, state.getBlockState());
        candidates = () -> Arrays.stream(states).map(BlockInfo::fromBlockState).toArray(BlockInfo[]::new);
        return this;
    }
}
