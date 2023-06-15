package com.gregtechceu.gtceu.common.data.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.generator.BiomePlacement;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
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
            var datagenExt = entry.getValue().veinGenerator();
            if (datagenExt != null) {
                Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, datagenExt.createConfiguredFeature());
            }
        }
        BiomeModifications.addFeature(
                ctx -> true,
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, GTCEu.id("ore"))
        );

        // rubber tree
        ResourceLocation id = GTCEu.id("trees_rubber");
        ResourceLocation vegetationId = GTCEu.id("rubber_vegetation");

        Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, vegetationId, new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(), GTPlacements.RUBBER_CHECKED)));
        Registry<ConfiguredFeature<?, ?>> featureRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
        Registry<Biome> biomeRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.BIOME_REGISTRY);
        var holder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, vegetationId));

        var placedFeature = new PlacedFeature(holder, List.of(
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(biomeRegistry.getOrCreateTag(CustomTags.IS_SWAMP), 50)
                )),
                PlacementUtils.countExtra(0, 0.005F, 1),
                InSquarePlacement.spread(), VegetationPlacements.TREE_THRESHOLD,
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(GTBlocks.RUBBER_SAPLING.getDefaultState(), BlockPos.ZERO)),
                BiomeFilter.biome()
        ));

        Registry.register(BuiltinRegistries.PLACED_FEATURE, id, placedFeature);
        ResourceKey<PlacedFeature> featureKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, id);

        BiomeModifications.addFeature(
                ctx -> ctx.hasTag(CustomTags.HAS_RUBBER_TREE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                featureKey
        );
    }
}
