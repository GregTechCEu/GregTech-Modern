package com.gregtechceu.gtceu.api.pattern.predicates;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;

public class PredicateFluidTag extends SimplePredicate {

    public TagKey<Fluid> tag = null;

    public PredicateFluidTag() {
        super("tags");
    }

    public PredicateFluidTag(TagKey<Fluid> tag) {
        this();
        this.tag = tag;
        buildPredicate();
    }

    @Override
    public SimplePredicate buildPredicate() {
        if (tag == null) {
            predicate = state -> false;
            candidates = () -> new BlockInfo[] { BlockInfo.fromBlock(Blocks.BARRIER) };
            return this;
        }
        predicate = state -> state.getBlockState().getFluidState().is(tag);
        candidates = () -> BuiltInRegistries.FLUID.getTag(tag)
                .stream()
                .flatMap(HolderSet.Named::stream)
                .map(Holder::value)
                .map(fluid -> BlockInfo.fromBlockState(fluid.defaultFluidState().createLegacyBlock()))
                .toArray(BlockInfo[]::new);
        return this;
    }
}
