package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.sodium;

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
        // don't bother checking if bloom can be loaded here; Embeddium won't load with OptiFine installed and shaders
        // aren't loaded when this class is loaded.
        // This mixin is also only applied if bloom safe mode is disabled.
        ALL = ArrayUtils.add(ALL, GTSodiumCompat.BLOOM_RENDER_PASS);
    }
}
