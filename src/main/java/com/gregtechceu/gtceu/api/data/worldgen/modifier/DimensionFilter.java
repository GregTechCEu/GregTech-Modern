package com.gregtechceu.gtceu.api.data.worldgen.modifier;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class DimensionFilter extends PlacementFilter {

    public static final PlacementModifierType<DimensionFilter> DIMENSION_FILTER = GTRegistries
            .register(BuiltInRegistries.PLACEMENT_MODIFIER_TYPE, GTCEu.id("dimension"), () -> DimensionFilter.CODEC);

    public static final Codec<DimensionFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.DIMENSION_TYPE).fieldOf("dimension_id")
                    .forGetter(filter -> filter.dimensionId))
            .apply(instance, DimensionFilter::new));

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
        return DIMENSION_FILTER;
    }
}
