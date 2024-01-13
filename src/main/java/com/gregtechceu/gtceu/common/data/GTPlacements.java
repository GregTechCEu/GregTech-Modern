package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.SurfaceWaterDepthFilter;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTPlacements
 */
public class GTPlacements {
    public static final ResourceKey<PlacedFeature> RUBBER_CHECKED = ResourceKey.create(Registries.PLACED_FEATURE, GTCEu.id("rubber_checked"));

    public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
        HolderGetter<Biome> biomeLookup = ctx.lookup(Registries.BIOME);

        PlacementUtils.register(ctx, RUBBER_CHECKED, featureLookup.getOrThrow(GTConfiguredFeatures.RUBBER),
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(() -> biomeLookup.getOrThrow(CustomTags.IS_SWAMP), 50)
                )),
                PlacementUtils.countExtra(0, ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance, 1),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_TOP_SOLID,
                BiomeFilter.biome(),
                PlacementUtils.filteredByBlockSurvival(GTBlocks.RUBBER_SAPLING.get())
        );
    }
}
