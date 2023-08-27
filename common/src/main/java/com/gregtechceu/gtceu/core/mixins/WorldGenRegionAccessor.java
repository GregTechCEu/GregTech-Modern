package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.server.level.WorldGenRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldGenRegion.class)
public interface WorldGenRegionAccessor {

    @Accessor
    int getWriteRadiusCutoff();
}
