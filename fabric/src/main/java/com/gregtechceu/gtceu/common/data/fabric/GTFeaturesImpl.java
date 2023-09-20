package com.gregtechceu.gtceu.common.data.fabric;

import com.gregtechceu.gtceu.common.data.GTPlacements;
import com.gregtechceu.gtceu.data.recipe.CustomTags;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.world.level.levelgen.GenerationStep;

/**
 * @author KilaBash
 * @date 2023/3/26
 * @implNote GTFeaturesImpl
 */
public class GTFeaturesImpl {
    public static void register() {
        // rubber tree
        BiomeModifications.addFeature(
                ctx -> ctx.hasTag(CustomTags.HAS_RUBBER_TREE),
                GenerationStep.Decoration.VEGETAL_DECORATION,
                GTPlacements.RUBBER_CHECKED
        );
    }
}
