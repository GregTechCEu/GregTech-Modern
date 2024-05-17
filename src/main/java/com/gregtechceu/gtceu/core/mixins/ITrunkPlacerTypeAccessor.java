package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import com.mojang.serialization.MapCodec;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TrunkPlacerType.class)
public interface ITrunkPlacerTypeAccessor {

    @Invoker(value = "<init>")
    static <T extends TrunkPlacer> TrunkPlacerType<T> callCtor(MapCodec<T> pCodec) {
        throw new NotImplementedException("Mixin failed to apply");
    }
}
