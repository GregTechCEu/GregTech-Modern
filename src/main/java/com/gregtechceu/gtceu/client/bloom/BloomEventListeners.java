package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import lombok.experimental.UtilityClass;

@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
@UtilityClass
public class BloomEventListeners {

    @SubscribeEvent
    public static void afterParticlesRendered(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        BloomRenderer.renderBloom(event.getCamera(), event.getPoseStack(), event.getFrustum(),
                event.getProjectionMatrix(),
                event.getPartialTick(), event.getLevelRenderer(), Minecraft.getInstance().getProfiler());
    }

    @SubscribeEvent
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || Minecraft.getInstance().level == null) return;
        if (!BloomShaderManager.isBloomActive()) return;

        BloomShaderManager.BLOOM_TARGET.clear(Minecraft.ON_OSX);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        BloomShaderManager.updateShaderAvailability(event);
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        BloomHandler.invalidateLevelData(event.getLevel());

        if (!BloomRenderer.usesChunkPassBackend()) {
            BloomRenderer.SafeMode.invalidateLevelData();
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!BloomShaderManager.isBloomActive()) return;

        ChunkAccess chunk = event.getChunk();
        LevelAccessor level = chunk.getWorldForge();
        if (level == null) return;

        if (BloomRenderer.usesChunkPassBackend()) return;

        ChunkPos chunkPos = chunk.getPos();
        int minSection = level.getMinSection(), maxSection = level.getMaxSection();
        for (int y = minSection; y < maxSection; y++) {
            BloomRenderer.SafeMode.invalidateSectionData(SectionPos.of(chunkPos.x, y, chunkPos.z));
        }
    }
}
