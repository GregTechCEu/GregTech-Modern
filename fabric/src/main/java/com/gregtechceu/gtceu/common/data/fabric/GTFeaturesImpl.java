package com.gregtechceu.gtceu.common.data.fabric;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.generator.BiomePlacement;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTFeaturesImpl
 */
public class GTFeaturesImpl {
    public static void register() {
        var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var featureRegistry = registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE);
        var biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
        var placedRegistry = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);

        //ores
        for (var entry : GTRegistries.ORE_VEINS.entries()) {
            ResourceLocation id = entry.getKey();
            var datagenExt = entry.getValue().getVeinGenerator();
            if (datagenExt != null) {
                Registry.register(featureRegistry, id, datagenExt.createConfiguredFeature());
            }
        }
        BiomeModifications.addFeature(
                ctx -> true,
                GenerationStep.Decoration.UNDERGROUND_ORES,
                ResourceKey.create(Registries.PLACED_FEATURE, GTCEu.id("ore"))
        );

        // rubber tree
        ResourceLocation id = GTCEu.id("trees_rubber");
        ResourceLocation vegetationId = GTCEu.id("rubber_vegetation");

        Registry.register(featureRegistry, vegetationId, new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(), placedRegistry.getHolderOrThrow(GTPlacements.RUBBER_CHECKED))));
        var holder = featureRegistry.getHolderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, vegetationId));

        var placedFeature = new PlacedFeature(holder, List.of(
                new BiomePlacement(List.of(
                        new BiomeWeightModifier(biomeRegistry.getOrCreateTag(CustomTags.IS_SWAMP), 50)
                )),
                PlacementUtils.countExtra(0, 0.005F, 1),
                InSquarePlacement.spread(),
                SurfaceWaterDepthFilter.forMaxDepth(0),
                PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                BlockPredicateFilter.forPredicate(BlockPredicate.wouldSurvive(GTBlocks.RUBBER_SAPLING.getDefaultState(), BlockPos.ZERO)),
                BiomeFilter.biome()
        ));

        Registry.register(placedRegistry, id, placedFeature);
        ResourceKey<PlacedFeature> featureKey = ResourceKey.create(Registries.PLACED_FEATURE, id);

        BiomeModifications.addFeature(
                ctx -> ctx.hasTag(CustomTags.HAS_RUBBER_TREE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                featureKey
        );
    }
}
