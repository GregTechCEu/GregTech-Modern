package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.api.data.worldgen.strata.IStrataLayer;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.holdersets.AnyHolderSet;

import java.util.List;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTFeaturesImpl
 */
public class GTFeaturesImpl {
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTER = DeferredRegister.create(Registry.CONFIGURED_FEATURE_REGISTRY, GTCEu.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURE_REGISTER = DeferredRegister.create(Registry.PLACED_FEATURE_REGISTRY, GTCEu.MOD_ID);
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.MOD_ID);

    public static void init(IEventBus modEventBus) {
        CONFIGURED_FEATURE_REGISTER.register(modEventBus);
        PLACED_FEATURE_REGISTER.register(modEventBus);
        BIOME_MODIFIER_REGISTER.register(modEventBus);
    }

    public static void register() {
        for (var entry : GTRegistries.ORE_VEINS.entries()) {
            ResourceLocation id = entry.getKey();
            var datagenExt = entry.getValue().getVeinGenerator();
            if (datagenExt != null) {
                CONFIGURED_FEATURE_REGISTER.register(id.getPath(), datagenExt::createConfiguredFeature);
            }
        }
        BIOME_MODIFIER_REGISTER.register("ore", () -> {
            Registry<Biome> biomeRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.BIOME_REGISTRY);
            Registry<PlacedFeature> featureRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            HolderSet<Biome> biomes = new AnyHolderSet<>(biomeRegistry);
            Holder<PlacedFeature> featureHolder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, GTCEu.id("ore")));
            return new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes,
                    HolderSet.direct(featureHolder),
                    GenerationStep.Decoration.UNDERGROUND_ORES
            );
        });

        // rubber tree
        ResourceLocation id = GTCEu.id("trees_rubber");
        ResourceLocation vegetationId = GTCEu.id("rubber_vegetation");

        CONFIGURED_FEATURE_REGISTER.register(vegetationId.getPath(), () -> new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(), GTPlacements.RUBBER_CHECKED)));
        PLACED_FEATURE_REGISTER.register(id.getPath(), () -> {
            Registry<ConfiguredFeature<?, ?>> featureRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.CONFIGURED_FEATURE_REGISTRY);
            Registry<Biome> biomeRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.BIOME_REGISTRY);
            var holder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, vegetationId));
            return new PlacedFeature(holder, List.of(
                    new BiomePlacement(List.of(
                            new BiomeWeightModifier(biomeRegistry.getOrCreateTag(CustomTags.IS_SWAMP), 50)
                    )),
                    PlacementUtils.countExtra(0, ConfigHolder.INSTANCE.worldgen.rubberTreeSpawnChance, 1),
                    InSquarePlacement.spread(),
                    VegetationPlacements.TREE_THRESHOLD,
                    PlacementUtils.HEIGHTMAP_OCEAN_FLOOR,
                    BiomeFilter.biome(),
                    PlacementUtils.filteredByBlockSurvival(GTBlocks.RUBBER_SAPLING.get())
            ));
        });

        BIOME_MODIFIER_REGISTER.register(id.getPath(), () -> {
            Registry<Biome> biomeRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.BIOME_REGISTRY);
            Registry<PlacedFeature> featureRegistry = BuiltinRegistries.ACCESS.registryOrThrow(Registry.PLACED_FEATURE_REGISTRY);
            HolderSet<Biome> biomes = new HolderSet.Named<>(biomeRegistry, CustomTags.HAS_RUBBER_TREE);
            Holder<PlacedFeature> featureHolder = featureRegistry.getOrCreateHolderOrThrow(ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, id));
            return new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes,
                    HolderSet.direct(featureHolder),
                    GenerationStep.Decoration.VEGETAL_DECORATION
            );
        });
    }
}
