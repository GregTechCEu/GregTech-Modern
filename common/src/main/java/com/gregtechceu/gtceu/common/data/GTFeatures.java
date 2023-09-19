package com.gregtechceu.gtceu.common.data;


import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomeFilter;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.BiomePlacement;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.DimensionFilter;
import com.gregtechceu.gtceu.api.data.worldgen.modifier.FrequencyModifier;
import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.resources.ResourceLocation;

/**
 * @author KilaBash
 * @date 2023/3/20
 * @implNote GTFeatures
 */
public class GTFeatures {
    public static final ResourceLocation NEW_ORE_VEIN_TOGGLE = GTCEu.id("vein_toggle");
    public static final ResourceLocation NEW_ORE_VEIN_RIDGED = GTCEu.id("vein_ridged");

    public static void init() {
        Object inst = FrequencyModifier.FREQUENCY_MODIFIER; // seemingly useless access to init the class in time
        inst = BiomeFilter.BIOME_FILTER;
        inst = DimensionFilter.DIMENSION_FILTER;
        inst = BiomePlacement.BIOME_PLACEMENT;
    }

    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }

}
