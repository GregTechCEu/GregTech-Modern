package com.gregtechceu.gtceu.api.data.worldgen.generator;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

import java.util.function.Predicate;

public class DimensionFilter extends PlacementFilter {
    public static PlacementModifierType<DimensionFilter> DIMENSION_FILTER = GTRegistries.register(Registry.PLACEMENT_MODIFIERS, GTCEu.id("dimension"), () -> DimensionFilter.CODEC);

    public static final Codec<DimensionFilter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registry.DIMENSION_TYPE_REGISTRY).fieldOf("dimension_id").forGetter(filter -> filter.dimensionId)
    ).apply(instance, DimensionFilter::new));

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
