package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.NeoForgeRenderTypes;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterNamedRenderTypesEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import lombok.experimental.UtilityClass;

@EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
@UtilityClass
public class BloomEventListeners {

    @SubscribeEvent
    public static void afterParticlesRendered(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        BloomRenderer.renderBloom(event.getCamera(), event.getPoseStack(), event.getFrustum(),
                event.getModelViewMatrix(), event.getProjectionMatrix(),
                event.getPartialTick().getGameTimeDeltaPartialTick(false), event.getLevelRenderer(),
                Minecraft.getInstance().getProfiler());
    }

    @SubscribeEvent
    public static void onRenderTick(RenderFrameEvent.Pre event) {
        if (Minecraft.getInstance().level == null) return;
        if (!BloomShaderManager.isBloomActive()) return;

        BloomShaderManager.BLOOM_TARGET.clear(Minecraft.ON_OSX);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        BloomHandler.invalidateLevelData(event.getLevel());

        if (BloomRenderer.SafeMode.enabled()) {
            BloomRenderer.SafeMode.invalidateLevelData();
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!BloomShaderManager.isBloomActive()) return;

        ChunkAccess chunk = event.getChunk();
        LevelAccessor level = chunk.getLevel();
        if (level == null) return;

        if (!BloomRenderer.SafeMode.enabled()) return;

        ChunkPos chunkPos = chunk.getPos();
        int minSection = level.getMinSection(), maxSection = level.getMaxSection();
        for (int y = minSection; y < maxSection; y++) {
            BloomRenderer.SafeMode.invalidateSectionData(SectionPos.of(chunkPos.x, y, chunkPos.z));
        }
    }

    @SubscribeEvent
    public static void registerNamedRenderTypes(RegisterNamedRenderTypesEvent event) {
        RenderType block, entity;
        if (!BloomRenderer.SafeMode.enabled() && BloomShaderManager.isBloomAvailable()) {
            block = GTRenderTypes.bloom();
            entity = GTRenderTypes.entityBloomBlockSheet();
        } else {
            // if safe mode is enabled, register the named render type as a copy of forge's 'cutout'
            block = RenderType.cutoutMipped();
            entity = NeoForgeRenderTypes.ITEM_LAYERED_CUTOUT.get();
        }
        event.register(GTCEu.id("bloom"), block, entity);
    }
}
