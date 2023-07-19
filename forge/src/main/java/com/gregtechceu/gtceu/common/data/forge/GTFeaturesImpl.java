package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.BiomeWeightModifier;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import com.gregtechceu.gtceu.api.data.worldgen.generator.BiomePlacement;
import com.gregtechceu.gtceu.common.data.GTBlocks;
import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
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
    public static final DeferredRegister<ConfiguredFeature<?, ?>> CONFIGURED_FEATURE_REGISTER = DeferredRegister.create(Registries.CONFIGURED_FEATURE, GTCEu.MOD_ID);
    public static final DeferredRegister<PlacedFeature> PLACED_FEATURE_REGISTER = DeferredRegister.create(Registries.PLACED_FEATURE, GTCEu.MOD_ID);
    public static final DeferredRegister<BiomeModifier> BIOME_MODIFIER_REGISTER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIERS, GTCEu.MOD_ID);

    public static void init(IEventBus modEventBus) {
        CONFIGURED_FEATURE_REGISTER.register(modEventBus);
        PLACED_FEATURE_REGISTER.register(modEventBus);
        BIOME_MODIFIER_REGISTER.register(modEventBus);
    }

    public static void register() {
        var registryAccess = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);

        for (var entry : GTOreFeatureEntry.ALL.entrySet()) {
            ResourceLocation id = entry.getKey();
            var datagenExt = entry.getValue().getVeinGenerator();
            if (datagenExt != null) {
                CONFIGURED_FEATURE_REGISTER.register(id.getPath(), datagenExt::createConfiguredFeature);
            }
        }
        BIOME_MODIFIER_REGISTER.register("ore", () -> {
            var biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME).asLookup();
            Registry<PlacedFeature> featureRegistry = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
            HolderSet<Biome> biomes = new AnyHolderSet<>(biomeRegistry);
            Holder<PlacedFeature> featureHolder = featureRegistry.getHolderOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, GTCEu.id("ore")));
            return new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes,
                    HolderSet.direct(featureHolder),
                    GenerationStep.Decoration.UNDERGROUND_ORES
            );
        });

        // rubber tree
        ResourceLocation id = GTCEu.id("trees_rubber");
        ResourceLocation vegetationId = GTCEu.id("rubber_vegetation");

        CONFIGURED_FEATURE_REGISTER.register(vegetationId.getPath(), () -> new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(),
                registryAccess.registryOrThrow(Registries.PLACED_FEATURE).getHolderOrThrow(GTPlacements.RUBBER_CHECKED))));
        PLACED_FEATURE_REGISTER.register(id.getPath(), () -> {
            Registry<ConfiguredFeature<?, ?>> featureRegistry = registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE);
            Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
            var holder = featureRegistry.getHolderOrThrow(ResourceKey.create(Registries.CONFIGURED_FEATURE, vegetationId));
            return new PlacedFeature(holder, List.of(
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
        });

        BIOME_MODIFIER_REGISTER.register(id.getPath(), () -> {
            Registry<Biome> biomeRegistry = registryAccess.registryOrThrow(Registries.BIOME);
            Registry<PlacedFeature> featureRegistry = registryAccess.registryOrThrow(Registries.PLACED_FEATURE);
            HolderSet<Biome> biomes = HolderSet.emptyNamed(biomeRegistry.holderOwner(), CustomTags.HAS_RUBBER_TREE);
            Holder<PlacedFeature> featureHolder = featureRegistry.getHolderOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, id));
            return new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                    biomes,
                    HolderSet.direct(featureHolder),
                    GenerationStep.Decoration.VEGETAL_DECORATION
            );
        });
    }
}
