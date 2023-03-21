package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.GTLayerOreFeature;
import com.gregtechceu.gtceu.api.data.worldgen.GTOreFeature;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import net.minecraft.core.Registry;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTFeatures
 */
public class GTFeatures {
    public static GTOreFeature ORE = GTRegistries.register(Registry.FEATURE, GTCEu.id("ore"), new GTOreFeature());
    public static GTLayerOreFeature LAYER_ORE = GTRegistries.register(Registry.FEATURE, GTCEu.id("layer_ore"), new GTLayerOreFeature());

    public static void init() {

    }

}
