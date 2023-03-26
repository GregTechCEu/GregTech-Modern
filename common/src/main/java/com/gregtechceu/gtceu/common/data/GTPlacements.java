package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTPlacements
 */
public class GTPlacements {
    public static final Holder<PlacedFeature> RUBBER_CHECKED = PlacementUtils.register(GTCEu.MOD_ID + ":rubber_checked",
            GTConfiguredFeatures.RUBBER, PlacementUtils.filteredByBlockSurvival(GTBlocks.RUBBER_SAPLING.get()));
}
