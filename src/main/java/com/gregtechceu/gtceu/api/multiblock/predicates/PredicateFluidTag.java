package com.gregtechceu.gtceu.api.multiblock.predicates;

import com.gregtechceu.gtceu.api.multiblock.Predicates;
import com.gregtechceu.gtceu.api.multiblock.util.BlockInfo;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PredicateFluidTag extends BasePredicate {

    public TagKey<Fluid> tag;

    public PredicateFluidTag(TagKey<Fluid> tag) {
        this(null, tag);
    }

    public PredicateFluidTag(@Nullable String debugName, TagKey<Fluid> tag) {
        Objects.requireNonNull(tag, "PredicateFluidTag tag cannot be null");

        this.tag = tag;

        errorPredicate = state -> state.retrieveCurrentBlockState().getFluidState().is(tag) ? null :
                Predicates.PLACEHOLDER;
        candidates = BuiltInRegistries.FLUID.getTag(tag)
                .stream()
                .flatMap(HolderSet.Named::stream)
                .map(Holder::value)
                .map(fluid -> BlockInfo.fromBlockState(fluid.defaultFluidState().createLegacyBlock()))
                .toList();

        this.debugName = Objects.requireNonNullElse(debugName, tag.registry().location() + "/" + tag.location());
    }
}
