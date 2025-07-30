package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class GTPlacements {

    public static final ResourceKey<PlacedFeature> RUBBER_CHECKED = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("rubber_checked"));
    public static final ResourceKey<PlacedFeature> RED_GRANITE_BLOB = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("red_granite_blob"));
    public static final ResourceKey<PlacedFeature> MARBLE_BLOB = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("marble_blob"));
    public static final ResourceKey<PlacedFeature> RAW_OIL_SPROUT = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("raw_oil_sprout"));

    public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);

        PlacementUtils.register(ctx, RUBBER_CHECKED, featureLookup.getOrThrow(GTConfiguredFeatures.RUBBER),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(() -> biomeLookup.getOrThrow(CustomTags.IS_SWAMP), 50))),
                RubberTreeChancePlacement.INSTANCE,
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(GTBlocks.RUBBER_SAPLING.get()));

        PlacementUtils.register(ctx, RED_GRANITE_BLOB, featureLookup.getOrThrow(GTConfiguredFeatures.RED_GRANITE_BLOB),
                RarityFilter.onAverageOnceEvery(10),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-8), VerticalAnchor.top()));
        PlacementUtils.register(ctx, MARBLE_BLOB, featureLookup.getOrThrow(GTConfiguredFeatures.MARBLE_BLOB),
                RarityFilter.onAverageOnceEvery(10),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(-8), VerticalAnchor.top()));
        PlacementUtils.register(ctx, RAW_OIL_SPROUT, featureLookup.getOrThrow(GTConfiguredFeatures.RAW_OIL_SPROUT),
                RarityFilter.onAverageOnceEvery(64),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(40)));
    }
}
