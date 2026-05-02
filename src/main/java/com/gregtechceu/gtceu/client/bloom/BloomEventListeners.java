package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraftforge.client.ForgeRenderTypes;
import net.minecraftforge.client.event.RegisterNamedRenderTypesEvent;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import lombok.experimental.UtilityClass;

import java.io.IOException;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
@UtilityClass
public class BloomEventListeners {

    @SubscribeEvent
    public static void afterParticlesRendered(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        BloomUtil.renderBloom(event.getCamera(), event.getPoseStack(), event.getFrustum(), event.getProjectionMatrix(),
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
        BloomUtil.invalidateLevelData(event.getLevel());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!BloomShaderManager.isBloomActive()) return;

        ChunkAccess chunk = event.getChunk();
        LevelAccessor level = chunk.getWorldForge();
        if (level == null) return;

        ChunkPos chunkPos = chunk.getPos();
        int minSection = level.getMinSection(), maxSection = level.getMaxSection();
        for (int y = minSection; y < maxSection; y++) {
            BloomUtil.invalidateSectionData(SectionPos.of(chunkPos.x, y, chunkPos.z));
        }
    }

    // Merge into parent class in 1.21, event listener discovery is smarter there
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    @UtilityClass
    public static class ModBus {

        @SubscribeEvent
        public void registerShaders(RegisterShadersEvent event) throws IOException {
            BloomShaderManager.onRegisterShaders(event);
        }

        @SubscribeEvent
        public void registerLevelRenderStages(RenderLevelStageEvent.RegisterStageEvent event) {
            BloomUtil.AFTER_BLOOM_RENDER_STAGE = event.register(GTCEu.id("after_bloom"), GTRenderTypes.bloom());
        }

        @SubscribeEvent
        public void registerNamedRenderTypes(RegisterNamedRenderTypesEvent event) {
            RenderType block, entity;
            if (GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SAFE_MODE_CONFIG_NAME) ||
                    !BloomShaderManager.isBloomAvailable()) {
                // if safe mode is enabled, register the named render type as a copy of forge's 'cutout'
                block = RenderType.cutout();
                entity = ForgeRenderTypes.ITEM_LAYERED_CUTOUT.get();
            } else {
                block = GTRenderTypes.bloom();
                entity = GTRenderTypes.entityBloomBlockSheet();
            }
            event.register("bloom", block, entity);
        }
    }
}
