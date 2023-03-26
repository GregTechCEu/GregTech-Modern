package com.gregtechceu.gtceu.common.data.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTFeaturesImpl
 */
public class GTFeaturesImpl {
    public static void register() {
        //ores
        for (var entry : GTOreFeatureEntry.ALL.entrySet()) {
            ResourceLocation id = entry.getKey();
            var datagenExt = entry.getValue().datagenExt();
            if (datagenExt != null) {
                Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, datagenExt.createConfiguredFeature(BuiltinRegistries.ACCESS));
                Registry.register(BuiltinRegistries.PLACED_FEATURE, id, datagenExt.createPlacedFeature(BuiltinRegistries.ACCESS));
                ResourceKey<PlacedFeature> featureKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, id);
                BiomeModifications.addFeature(
                        ctx -> ctx.hasTag(datagenExt.biomeTag),
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        featureKey
                );
            }
        }

        // rubber tree
        var id = GTCEu.id("rubber_tree");
        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(), GTPlacements.RUBBER_CHECKED)));
        Registry<ConfiguredFeature<?, ?>> featureRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
        var holder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, id));

        var placedFeature = new PlacedFeature(holder, List.of(
                PlacementUtils.countExtra(0, 0.005F, 1),
                InSquarePlacement.spread(), VegetationPlacements.TREE_THRESHOLD,
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(Blocks.OAK_SAPLING.defaultBlockState(), BlockPos.ZERO)),
                BiomeFilter.biome()
        ));

        Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature);
        ResourceKey<PlacedFeature> featureKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, id);

        BiomeModifications.addFeature(
                ctx -> ctx.hasTag(BiomeTags.IS_OVERWORLD),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                featureKey
        );
    }
}
