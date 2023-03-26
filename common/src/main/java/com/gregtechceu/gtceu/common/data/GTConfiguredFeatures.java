package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.worldgen.RubberFoliagePlacer;
import com.gregtechceu.gtceu.common.worldgen.RubberTrunkPlacer;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

import java.util.OptionalInt;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTConfiguredFeatures
 */
public class GTConfiguredFeatures {
    public static final Holder<ConfiguredFeature<TreeConfiguration, ?>> RUBBER = FeatureUtils.register(GTCEu.MOD_ID + ":rubber", Feature.TREE,
            new TreeConfiguration.TreeConfigurationBuilder(
                    BlockStateProvider.simple(GTBlocks.RUBBER_LOG.get().changeNatural(GTBlocks.RUBBER_LOG.getDefaultState(), true)),
                    new RubberTrunkPlacer(11, 3, 0),
                    BlockStateProvider.simple(GTBlocks.RUBBER_LEAVES.get()),
                    new RubberFoliagePlacer(ConstantInt.of(5), ConstantInt.of(2)),
                    new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))).ignoreVines().build());

//    public static final Holder<ConfiguredFeature<RandomFeatureConfiguration,?>> TREES_ADDITIONS = FeatureUtils.register(GTCEu.MOD_ID + ":trees_additions", Feature.RANDOM_SELECTOR,
//            new RandomFeatureConfiguration(List.of(), GTPlacements.RUBBER_CHECKED));
}
