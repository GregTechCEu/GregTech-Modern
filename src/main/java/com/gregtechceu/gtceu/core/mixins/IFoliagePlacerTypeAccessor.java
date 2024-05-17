package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacerType;

import com.mojang.serialization.MapCodec;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FoliagePlacerType.class)
public interface IFoliagePlacerTypeAccessor {

    @Invoker(value = "<init>")
    static <T extends FoliagePlacer> FoliagePlacerType<T> callCtor(MapCodec<T> pCodec) {
        throw new NotImplementedException("Mixin failed to apply");
    }
}
