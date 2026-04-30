package com.gregtechceu.gtceu.core.mixins.embeddium.bloom;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;

import net.minecraft.client.renderer.RenderType;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.List;
import java.util.Map;

@Mixin(value = DefaultTerrainRenderPasses.class, remap = false)
public class DefaultTerrainRenderPassesMixin {

    // spotless:off

    @Definition(id = "ALL", field = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/DefaultTerrainRenderPasses;ALL:[Lme/jellysquid/mods/sodium/client/render/chunk/terrain/TerrainRenderPass;")
    @Expression("ALL = @(?)") // technically quite brittle. I don't think it matters much here, though.
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static TerrainRenderPass[] gtceu$forceAddBloomToTerrainRenderPasses$1(TerrainRenderPass[] original) {
        // TODO add a way to conditionally load mixins based on configs
        if (ConfigHolder.INSTANCE.client.bloom.safeMode) return original;
        if (!GTShaders.canUseBloomShader()) return original;

        return ArrayUtils.add(original, GTEmbeddiumCompat.BLOOM_RENDER_PASS);
    }

    @Definition(id = "RENDER_PASS_MAPPINGS", field = "Lme/jellysquid/mods/sodium/client/render/chunk/terrain/DefaultTerrainRenderPasses;RENDER_PASS_MAPPINGS:Ljava/util/Map;")
    @Expression("RENDER_PASS_MAPPINGS = @(?)") // technically quite brittle. I don't think it matters much here, though.
    @ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static Map<RenderType, List<TerrainRenderPass>> gtceu$forceAddBloomToTerrainRenderPasses$2(Map<RenderType, List<TerrainRenderPass>> original) {
        // TODO add a way to conditionally load mixins based on configs
        if (ConfigHolder.INSTANCE.client.bloom.safeMode) return original;
        if (!GTShaders.canUseBloomShader()) return original;

        return ImmutableMap.<RenderType, List<TerrainRenderPass>>builder()
                .putAll(original)
                .put(GTRenderTypes.bloom(), List.of(GTEmbeddiumCompat.BLOOM_RENDER_PASS))
                .build();
    }

    // spotless:on
}
