package com.gregtechceu.gtceu.api.pattern.predicates;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Objects;

public class PredicateFluids extends SimplePredicate {

    public Fluid[] fluids = new Fluid[0];

    public PredicateFluids() {
        super("fluids");
    }

    public PredicateFluids(Fluid... fluids) {
        this();
        this.fluids = fluids;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        fluids = Arrays.stream(fluids).filter(Objects::nonNull).toArray(Fluid[]::new);
        if (fluids.length == 0) fluids = new Fluid[] { Fluids.WATER };
        predicate = state -> ArrayUtils.contains(fluids, state.getBlockState().getFluidState().getType());
        candidates = () -> Arrays.stream(fluids)
                .map(fluid -> BlockInfo.fromBlockState(fluid.defaultFluidState().createLegacyBlock()))
                .toArray(BlockInfo[]::new);
        return this;
    }
}
