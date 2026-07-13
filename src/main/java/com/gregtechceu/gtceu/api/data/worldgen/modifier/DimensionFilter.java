package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import com.gregtechceu.gtceu.common.data.worldgen.GTPlacementModifiers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class DimensionFilter extends PlacementFilter {

    // spotless:off
    public static final Codec<DimensionFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.DIMENSION_TYPE).fieldOf("dimension_id").forGetter(filter -> filter.dimensionId)
    ).apply(instance, DimensionFilter::new));
    // spotless:on

    public HolderSet<DimensionType> dimensionId;

    public DimensionFilter(HolderSet<DimensionType> dimensionId) {
        this.dimensionId = dimensionId;
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        return this.dimensionId.contains(context.getLevel().getLevel().dimensionTypeRegistration());
    }

    @Override
    public PlacementModifierType<?> type() {
        return GTPlacementModifiers.DIMENSION_FILTER.get();
    }
}
