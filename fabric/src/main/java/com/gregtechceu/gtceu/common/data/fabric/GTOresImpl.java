package com.gregtechceu.gtceu.common.data.fabric;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeatureEntry;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTOresImpl
 */
public class GTOresImpl {
    public static void register() {
        for (var entry : GTOreFeatureEntry.ALL.entrySet()) {
            ResourceLocation id = entry.getKey();
            var datagenExt = entry.getValue().datagenExt();
            if (datagenExt != null) {
                Registry.register(BuiltinRegistries.CONFIGURED_FEATURE, id, datagenExt.createConfiguredFeature(BuiltinRegistries.ACCESS));
                Registry.register(BuiltinRegistries.PLACED_FEATURE, id, datagenExt.createPlacedFeature(BuiltinRegistries.ACCESS));
                ResourceKey<PlacedFeature> featureKey = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, id);
                BiomeModifications.addFeature(
                        ctx -> ctx.hasTag(datagenExt.biomeTag),
                        GenerationStep.Decoration.UNDERGROUND_ORES,
                        featureKey
                );
            }
        }
    }
}
