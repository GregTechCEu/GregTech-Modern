package com.gregtechceu.gtceu.core.mixins.client.bloom.normal.embeddium;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.RenderType;

import com.google.common.collect.ImmutableMap;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public class DefaultTerrainRenderPassesMixin {

    @Shadow
    @Final
    @Mutable
    public static TerrainRenderPass[] ALL;

    @Shadow
    @Final
    @Mutable
    public static Map<RenderType, List<TerrainRenderPass>> RENDER_PASS_MAPPINGS;

    static {
        if (GTShaders.isBloomShaderAvailable()) {
            ALL = ArrayUtils.add(ALL, GTEmbeddiumCompat.BLOOM_RENDER_PASS);

            RENDER_PASS_MAPPINGS = ImmutableMap.<RenderType, List<TerrainRenderPass>>builder()
                    .putAll(RENDER_PASS_MAPPINGS)
                    .put(GTRenderTypes.bloom(), List.of(GTEmbeddiumCompat.BLOOM_RENDER_PASS))
                    .build();
        }
    }
}
