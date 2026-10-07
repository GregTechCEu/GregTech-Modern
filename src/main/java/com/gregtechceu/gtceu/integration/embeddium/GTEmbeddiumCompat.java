package com.gregtechceu.gtceu.integration.embeddium;

import com.gregtechceu.gtceu.client.bloom.BloomRenderer;
import com.gregtechceu.gtceu.client.bloom.BloomShaderManager;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.SectionPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import lombok.Getter;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import me.jellysquid.mods.sodium.client.render.texture.SpriteUtil;
import org.embeddedt.embeddium.api.ChunkMeshEvent;

import java.util.Collection;

public class GTEmbeddiumCompat {

    @Getter(lazy = true)
    private static final TerrainRenderPass bloomRenderPass = new TerrainRenderPass(GTRenderTypes.bloom(), false, true);
    @Getter(lazy = true)
    private static final Material bloomMaterial = new Material(getBloomRenderPass(), AlphaCutoffParameter.ZERO, true);

    public static void init() {
        MinecraftForge.EVENT_BUS.register(GTEmbeddiumCompat.class);
    }

    @SubscribeEvent
    public static void registerSafeModeChunkMeshAppender(ChunkMeshEvent event) {
        if (!BloomRenderer.SafeMode.enabled()) return;
        if (!BloomShaderManager.isBloomActive()) return;

        event.addMeshAppender(context -> {
            SectionPos sectionOrigin = context.sectionOrigin();
            if (!BloomRenderer.SafeMode.BLOOM_BUFFER_BUILDERS.containsKey(sectionOrigin)) {
                return;
            }

            Vec3 camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            BloomRenderer.SafeMode.bakeBloomChunkBuffers(sectionOrigin,
                    (float) camPos.x, (float) camPos.y, (float) camPos.z);
        });
    }

    public static void markSpritesAsActive(Collection<TextureAtlasSprite> sprites) {
        for (TextureAtlasSprite sprite : sprites) {
            SpriteUtil.markSpriteActive(sprite);
        }
    }
}
