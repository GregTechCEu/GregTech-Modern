package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.FluidSproutConfiguration;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.StoneBlobConfiguration;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class GTConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER = ResourceKey.create(Registries.CONFIGURED_FEATURE,
            GTCEu.id("rubber_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> RED_GRANITE_BLOB = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, GTCEu.id("red_granite_blob"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> MARBLE_BLOB = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, GTCEu.id("marble_blob"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> RAW_OIL_SPROUT = ResourceKey
            .create(Registries.CONFIGURED_FEATURE, GTCEu.id("raw_oil_sprout"));

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> ctx) {
        FeatureUtils.register(ctx, RUBBER, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider
                        .simple(GTBlocks.RUBBER_LOG.get().changeNatural(GTBlocks.RUBBER_LOG.getDefaultState(), true)),
                new ForkingTrunkPlacer(5, 1, 3),
                BlockStateProvider.simple(GTBlocks.RUBBER_LEAVES.get()),
                new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
                new TwoLayersFeatureSize(1, 0, 2)).ignoreVines().build());

        FeatureUtils.register(ctx, RED_GRANITE_BLOB, GTFeatures.STONE_BLOB.get(),
                new StoneBlobConfiguration(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        GTBlocks.RED_GRANITE.getDefaultState()), UniformInt.of(20, 30)));
        FeatureUtils.register(ctx, MARBLE_BLOB, GTFeatures.STONE_BLOB.get(),
                new StoneBlobConfiguration(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES),
                        GTBlocks.MARBLE.getDefaultState()), UniformInt.of(20, 30)));

        FeatureUtils.register(ctx, RAW_OIL_SPROUT, GTFeatures.FLUID_SPROUT.get(),
                new FluidSproutConfiguration(GTMaterials.RawOil.getFluid(), UniformInt.of(9, 13), UniformInt.of(6, 9),
                        0.4f));
    }
}
