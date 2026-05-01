package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomSafeMode;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;

import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import org.embeddedt.embeddium.api.ChunkMeshEvent;

public class GTEmbeddiumCompat {

    public static final TerrainRenderPass BLOOM_RENDER_PASS = TerrainRenderPass.builder()
            .layer(GTRenderTypes.bloom())
            .fragmentDiscard(true)
            .build();
    public static final Material BLOOM_MATERIAL = new Material(BLOOM_RENDER_PASS,
            AlphaCutoffParameter.ONE_TENTH, false);

    public static void init() {
        MinecraftForge.EVENT_BUS.register(GTEmbeddiumCompat.class);
    }

    @SubscribeEvent
    public static void registerSafeModeChunkMeshAppender(ChunkMeshEvent event) {
        if (!GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SAFE_MODE_CONFIG_NAME)) return;
        if (!BloomShaderManager.isBloomShaderInUse()) return;

        event.addMeshAppender(context -> {
            SectionPos sectionOrigin = context.sectionOrigin();
            if (!BloomSafeMode.BLOOM_BUFFER_BUILDERS.containsKey(sectionOrigin)) {
                return;
            }

            Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            BloomSafeMode.CURRENT_RENDERING_SECTION.set(sectionOrigin);
            BloomSafeMode.bakeBloomChunkBuffers(sectionOrigin, camPos);
        });
    }
}
