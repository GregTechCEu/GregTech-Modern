package com.gregtechceu.gtceu.api.pattern.predicates;

import com.lowdragmc.lowdraglib.utils.BlockInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class PredicateBlockTag extends SimplePredicate {

    public TagKey<Block> tag = null;

    public PredicateBlockTag() {
        super("tags");
    }

    public PredicateBlockTag(TagKey<Block> tag) {
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
        predicate = state -> state.getBlockState().is(tag);
        candidates = () -> BuiltInRegistries.BLOCK.getTag(tag)
                .stream()
                .flatMap(HolderSet.Named::stream)
                .map(Holder::value)
                .map(BlockInfo::fromBlock)
                .toArray(BlockInfo[]::new);
        return this;
    }
}
