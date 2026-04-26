package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;

import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
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
    public static void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.START || Minecraft.getInstance().level == null) {
            return;
        }
        if (!GTShaders.canUseBloomShader()) {
            return;
        }

        GTShaders.BLOOM_TARGET.clear(Minecraft.ON_OSX);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        GTShaders.updateShaderAvailability(event);
    }

    @SubscribeEvent
    public static void onChunkUnloadEvent(ChunkEvent.Unload event) {
        if (!GTShaders.canUseBloomShader() || GTCEu.Mods.isSodiumEmbeddiumLoaded()) {
            return;
        }
        ChunkAccess chunk = event.getChunk();
        LevelAccessor level = chunk.getWorldForge();
        if (level == null) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        for (int y = level.getMinSection(); y < level.getMaxSection(); y++) {
            BloomUtil.chunkSectionUnloaded(SectionPos.of(chunkPos, y));
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        BloomUtil.invalidateLevelTickets(event.getLevel());
    }

    // Merge into parent class in 1.21, event listener discovery is smarter there
    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    @UtilityClass
    public static class ModBus {

        @SubscribeEvent
        public void registerShaders(RegisterShadersEvent event) throws IOException {
            GTShaders.onRegisterShaders(event);
        }

        @SubscribeEvent
        public void registerLevelRenderStages(RenderLevelStageEvent.RegisterStageEvent event) {
            BloomUtil.AFTER_BLOOM_RENDER_STAGE = event.register(GTCEu.id("after_bloom"), GTRenderTypes.bloom());
        }

        @SubscribeEvent
        public void registerNamedRenderTypes(RegisterNamedRenderTypesEvent event) {
            event.register("bloom", GTRenderTypes.bloom(), GTRenderTypes.entityBloomBlockSheet());
        }
    }
}
