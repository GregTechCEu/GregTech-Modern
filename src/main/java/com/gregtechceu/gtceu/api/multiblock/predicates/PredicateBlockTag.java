package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PredicateBlockTag extends BasePredicate {

    public TagKey<Block> tag;

    public PredicateBlockTag(TagKey<Block> tag) {
        this(null, tag);
    }

    public PredicateBlockTag(@Nullable String debugName, TagKey<Block> tag) {
        Objects.requireNonNull(tag, "PredicateBlockTag tag cannot be null");
        this.tag = tag;

        errorPredicate = state -> state.retrieveCurrentBlockState().is(tag) ? null : Predicates.PLACEHOLDER;
        candidates = BuiltInRegistries.BLOCK.getTag(tag)
                .stream()
                .flatMap(HolderSet.Named::stream)
                .map(Holder::value)
                .map(BlockInfo::fromBlock)
                .toList();

        if (debugName == null) {
            this.debugName = tag.registry().location() + "/" + tag.location();
        } else {
            this.debugName = debugName;
        }
    }
}
