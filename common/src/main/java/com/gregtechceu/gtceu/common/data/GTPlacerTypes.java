package com.gregtechceu.gtceu.common.data;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.worldgen.RubberFoliagePlacer;
import com.gregtechceu.gtceu.common.worldgen.RubberTrunkPlacer;
import com.gregtechceu.gtceu.core.mixins.IFoliagePlacerTypeAccessor;
import com.gregtechceu.gtceu.core.mixins.ITrunkPlacerTypeAccessor;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

/**
 * @author KilaBash
 * @date 2023/3/25
 * @implNote GTPlacerTypes
 */
public class GTPlacerTypes {

    public static final TrunkPlacerType<RubberTrunkPlacer> RUBBER_TRUNK = registerTruckPlacer("rubber", RubberTrunkPlacer.CODEC);
    public static final FoliagePlacerType<RubberFoliagePlacer> RUBBER_FOLIAGE = registerFoliagePlacer("rubber", RubberFoliagePlacer.CODEC);

    public static <P extends TrunkPlacer> TrunkPlacerType<P> registerTruckPlacer(String pKey, Codec<P> pCodec) {
        return GTRegistries.register(BuiltInRegistries.TRUNK_PLACER_TYPE, GTCEu.id(pKey), ITrunkPlacerTypeAccessor.callCtor(pCodec));
    }

    public static <P extends FoliagePlacer> FoliagePlacerType<P> registerFoliagePlacer(String pKey, Codec<P> pCodec) {
        return GTRegistries.register(BuiltInRegistries.FOLIAGE_PLACER_TYPE, GTCEu.id(pKey), IFoliagePlacerTypeAccessor.callCtor(pCodec));
    }

    public static void init() {

    }
}
