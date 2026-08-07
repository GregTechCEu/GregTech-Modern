package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.StringJoiner;

public class PredicateFluids extends BasePredicate {

    public final Fluid[] fluids;

    public PredicateFluids(Fluid... fluids) {
        this(null, fluids);
    }

    public PredicateFluids(@Nullable String debugName, Fluid... fluids) {
        Validate.noNullElements(fluids, "Fluids array has null element at index %s");

        if (fluids.length == 0) this.fluids = new Fluid[] { Fluids.WATER };
        else this.fluids = Arrays.stream(fluids).toArray(Fluid[]::new);
        errorPredicate = state -> ArrayUtils.contains(this.fluids,
                state.retrieveCurrentBlockState().getFluidState().getType()) ?
                        null : Predicates.PLACEHOLDER;
        candidates = Arrays.stream(this.fluids)
                .map(fluid -> BlockInfo.fromBlockState(fluid.defaultFluidState().createLegacyBlock()))
                .toList();

        if (debugName == null) {
            StringJoiner sb = new StringJoiner("-");
            for (Fluid f : fluids) {
                sb.add(BuiltInRegistries.FLUID.getKey(f).getPath());
            }
            this.debugName = sb.toString();
        } else {
            this.debugName = debugName;
        }
    }
}
