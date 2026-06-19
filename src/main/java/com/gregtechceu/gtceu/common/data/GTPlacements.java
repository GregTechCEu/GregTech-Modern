package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.modifier.RubberTreeChancePlacement;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.BiasedToBottomInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;

public class GTPlacements {

    public static final ResourceKey<PlacedFeature> RUBBER_TREE = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("rubber_tree"));
    public static final ResourceKey<PlacedFeature> RUBBER_TREE_SWAMP = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("rubber_tree_swamp"));
    public static final ResourceKey<PlacedFeature> RED_GRANITE_BLOB = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("red_granite_blob"));
    public static final ResourceKey<PlacedFeature> MARBLE_BLOB = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("marble_blob"));
    public static final ResourceKey<PlacedFeature> RAW_OIL_SPROUT = ResourceKey.create(Registries.PLACED_FEATURE,
            GTCEu.id("raw_oil_sprout"));

    public static void bootstrap(BootstrapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);

        Holder<ConfiguredFeature<?, ?>> rubberTreeConfig = featureLookup.getOrThrow(GTConfiguredFeatures.RUBBER_TREE);
        PlacementUtils.register(ctx, RUBBER_TREE, rubberTreeConfig,
                RubberTreeChancePlacement.INSTANCE,
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(GTBlocks.RUBBER_SAPLING.get()));
        PlacementUtils.register(ctx, RUBBER_TREE_SWAMP, rubberTreeConfig,
                RubberTreeChancePlacement.INSTANCE,
                CountPlacement.of(BiasedToBottomInt.of(0, 4)),
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
                RarityFilter.onAverageOnceEvery(128),
                InSquarePlacement.spread(),
                BiomeFilter.biome(),
                HeightRangePlacement.uniform(VerticalAnchor.absolute(10), VerticalAnchor.absolute(40)));
    }
}
