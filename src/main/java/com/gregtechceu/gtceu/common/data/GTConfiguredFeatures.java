package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.RubberFoliagePlacer;
import com.gregtechceu.gtceu.common.worldgen.RubberTrunkPlacer;
import com.gregtechceu.gtceu.common.worldgen.feature.configurations.StoneBlobConfiguration;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
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

import java.util.OptionalInt;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTConfiguredFeatures
 */
public class GTConfiguredFeatures {
    public static final Holder<ConfiguredFeature<TreeConfiguration, ?>> RUBBER = FeatureUtils.register(GTCEu.MOD_ID + ":rubber_tree", Feature.TREE,
        new TreeConfiguration.TreeConfigurationBuilder(
            BlockStateProvider.simple(GTBlocks.RUBBER_LOG.get().changeNatural(GTBlocks.RUBBER_LOG.getDefaultState(), true)),
            new ForkingTrunkPlacer(5, 1, 3),
            BlockStateProvider.simple(GTBlocks.RUBBER_LEAVES.get()),
            new MegaJungleFoliagePlacer(ConstantInt.of(1), UniformInt.of(0, 1), 1),
            new TwoLayersFeatureSize(1, 0, 2)).ignoreVines().build());
    public static final Holder<ConfiguredFeature<StoneBlobConfiguration, ?>> RED_GRANITE_BLOB = FeatureUtils.register(GTCEu.MOD_ID + ":red_granite_blob", GTFeatures.STONE_BLOB.get(),
        new StoneBlobConfiguration(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), GTBlocks.RED_GRANITE.getDefaultState()), UniformInt.of(20, 30)));
    public static final Holder<ConfiguredFeature<StoneBlobConfiguration, ?>> MARBLE_BLOB = FeatureUtils.register(GTCEu.MOD_ID + ":marble_blob", GTFeatures.STONE_BLOB.get(),
        new StoneBlobConfiguration(OreConfiguration.target(new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES), GTBlocks.MARBLE.getDefaultState()), UniformInt.of(20, 30)));
}
