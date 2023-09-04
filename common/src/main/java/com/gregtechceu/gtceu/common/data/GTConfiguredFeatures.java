package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureConfiguration;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.Noises;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.ForkingTrunkPlacer;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTConfiguredFeatures
 */
public class GTConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER = ResourceKey.create(Registries.CONFIGURED_FEATURE, GTCEu.id("rubber_tree"));
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE = ResourceKey.create(Registries.CONFIGURED_FEATURE, GTCEu.id("ore"));

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> ctx) {
        FeatureUtils.register(ctx, RUBBER, Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(GTBlocks.RUBBER_LOG.get().changeNatural(GTBlocks.RUBBER_LOG.getDefaultState(), true)),
                new ForkingTrunkPlacer(5, 1, 3),
                BlockStateProvider.simple(GTBlocks.RUBBER_LEAVES.get()),
                new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
                new TwoLayersFeatureSize(1, 0, 2)).ignoreVines().build());
        FeatureUtils.register(ctx, ORE, GTFeatures.ORE, new GTOreFeatureConfiguration());
    }
}
