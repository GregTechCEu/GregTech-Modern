package com.gregtechceu.gtceu.core.mixins.client.customchunk.sodium;

import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public class DefaultTerrainRenderPassesMixin {

    @Shadow
    @Final
    @Mutable
    public static TerrainRenderPass[] ALL;

    static {
        TerrainRenderPass[] customPasses = GTSodiumCompat.getCustomRenderPasses().values()
                .toArray(TerrainRenderPass[]::new);
        ALL = ArrayUtils.addAll(ALL, customPasses);
    }
}
