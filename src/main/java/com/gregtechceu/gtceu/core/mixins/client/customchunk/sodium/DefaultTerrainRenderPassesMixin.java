package com.gregtechceu.gtceu.core.mixins.client.customchunk.sodium;

import com.gregtechceu.gtceu.client.renderer.CustomChunkRenderPassRegistry;
import com.gregtechceu.gtceu.integration.sodium.GTSodiumCompat;

import net.caffeinemc.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import net.caffeinemc.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;

import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public class DefaultTerrainRenderPassesMixin {

    @Shadow
    @Final
    @Mutable
    public static TerrainRenderPass[] ALL;

    static {
        TerrainRenderPass[] customPasses = CustomChunkRenderPassRegistry.activePasses().stream()
                .map(pass -> GTSodiumCompat.getCustomRenderPass(pass.renderType()))
                .filter(pass -> Arrays.stream(ALL).noneMatch(existing -> existing == pass))
                .toArray(TerrainRenderPass[]::new);
        ALL = ArrayUtils.addAll(ALL, customPasses);
    }
}
