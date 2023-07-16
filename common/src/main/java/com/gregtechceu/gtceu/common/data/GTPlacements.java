package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.simibubi.create.Create;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTPlacements
 */
public class GTPlacements {
    public static final ResourceKey<PlacedFeature> RUBBER_CHECKED = ResourceKey.create(Registries.PLACED_FEATURE, GTCEu.id("rubber_checked"));

    public static void bootstrap(BootstapContext<PlacedFeature> ctx) {
        HolderGetter<ConfiguredFeature<?, ?>> featureLookup = ctx.lookup(Registries.CONFIGURED_FEATURE);
        PlacementUtils.register(ctx, RUBBER_CHECKED, featureLookup.getOrThrow(GTConfiguredFeatures.RUBBER), PlacementUtils.filteredByBlockSurvival(GTBlocks.RUBBER_SAPLING.get()));
    }
}
