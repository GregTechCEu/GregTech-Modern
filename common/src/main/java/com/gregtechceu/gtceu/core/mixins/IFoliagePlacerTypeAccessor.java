package com.gregtechceu.gtceu.core.mixins;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FoliagePlacerType.class)
public interface IFoliagePlacerTypeAccessor {

    @Invoker(value = "<init>")
    static <T extends FoliagePlacer> FoliagePlacerType<T> callCtor(Codec<T> pCodec) {
        throw new NotImplementedException("Mixin failed to apply");
    }
}
