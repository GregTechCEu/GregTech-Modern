package com.gregtechceu.gtceu.client.particle;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.bloom.EffectRenderContext;
import com.gregtechceu.gtceu.client.bloom.IRenderSetup;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Singleton class responsible for managing, updating and rendering {@link GTParticle} instances.
 */
@Mod.EventBusSubscriber(modid = GTCEu.MOD_ID, value = Dist.CLIENT)
public class GTParticleManager {

    public static final GTParticleManager INSTANCE = new GTParticleManager();

    private final Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> depthEnabledParticles = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> depthDisabledParticles = new Object2ObjectLinkedOpenHashMap<>();

    private final List<GTParticle> newParticleQueue = new ArrayList<>();

    public void addEffect(GTParticle particles) {
        newParticleQueue.add(particles);
    }

    public void updateEffects() {
        if (!this.depthEnabledParticles.isEmpty()) {
            updateQueue(this.depthEnabledParticles);
        }
        if (!this.depthDisabledParticles.isEmpty()) {
            updateQueue(this.depthDisabledParticles);
        }

        if (!newParticleQueue.isEmpty()) {
            for (GTParticle particle : newParticleQueue) {
                var queue = particle.shouldDisableDepth() ? this.depthDisabledParticles : this.depthEnabledParticles;

                ArrayDeque<GTParticle> particles = queue.computeIfAbsent(particle.getRenderSetup(),
                        setup -> new ArrayDeque<>());

                if (particles.size() > 6000) {
                    particles.removeFirst().setExpired();
                }
                particles.add(particle);
            }

            newParticleQueue.clear();
        }
    }

    private void updateQueue(Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> renderQueue) {
        Iterator<ArrayDeque<GTParticle>> it = renderQueue.values().iterator();
        while (it.hasNext()) {
            ArrayDeque<GTParticle> particlesForSetup = it.next();

            Iterator<GTParticle> particles = particlesForSetup.iterator();
            while (particles.hasNext()) {
                GTParticle particle = particles.next();

                if (particle.isAlive()) {
                    try {
                        particle.onUpdate();
                    } catch (RuntimeException exception) {
                        GTCEu.LOGGER.error("Particle update error: {}", particle, exception);
                        particle.setExpired();
                    }
                    if (particle.isAlive()) continue;
                }

                particles.remove();
            }

            if (particlesForSetup.isEmpty()) {
                it.remove();
            }
        }
    }

    public void clearAllEffects(boolean cleanNewQueue) {
        if (cleanNewQueue) {
            for (GTParticle particle : this.newParticleQueue) {
                particle.setExpired();
            }
            this.newParticleQueue.clear();
        }
        for (ArrayDeque<GTParticle> particles : this.depthEnabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        for (ArrayDeque<GTParticle> particles : this.depthDisabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        this.depthEnabledParticles.clear();
        this.depthDisabledParticles.clear();
    }

    public void renderParticles(PoseStack poseStack, Camera camera, Frustum frustum, float partialTicks) {
        if (this.depthEnabledParticles.isEmpty() && this.depthDisabledParticles.isEmpty()) return;

        EffectRenderContext instance = EffectRenderContext.getInstance()
                .update(camera, frustum, partialTicks);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if (!this.depthDisabledParticles.isEmpty()) {
            RenderSystem.depthMask(false);
            renderParticlesInLayer(poseStack, this.depthDisabledParticles, instance);
            RenderSystem.depthMask(true);
        }
        renderParticlesInLayer(poseStack, this.depthEnabledParticles, instance);

        RenderSystem.disableBlend();
    }

    private static void renderParticlesInLayer(PoseStack poseStack,
                                               Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> renderQueue,
                                               EffectRenderContext context) {
        for (var entry : renderQueue.entrySet()) {
            ArrayDeque<GTParticle> particles = entry.getValue();
            if (particles.isEmpty()) continue;

            IRenderSetup handler = entry.getKey();
            boolean initialized = false;

            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            for (GTParticle particle : particles) {
                if (!particle.shouldRender(context)) {
                    continue;
                }
                try {
                    if (!initialized) {
                        initialized = true;
                        if (handler != null) {
                            handler.preDraw(buffer);
                        }
                    }
                    particle.renderParticle(poseStack, buffer, context);

                } catch (Throwable throwable) {
                    GTCEu.LOGGER.error("Particle render error: {}", particle, throwable);
                    particle.setExpired();
                }
            }
            if (initialized && handler != null) {
                handler.postDraw(buffer);
            }
        }
    }

    @SubscribeEvent
    public static void onClientLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel newLevel)) {
            return;
        }
        ClientLevel oldLevel = Minecraft.getInstance().level;
        if (oldLevel != newLevel) {
            INSTANCE.clearAllEffects(oldLevel != null);
        }

        if (oldLevel != null) {
            INSTANCE.updateEffects();
        }
    }

    @SubscribeEvent
    public static void onClientLevelUnload(ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getPlayer() != null) {
            INSTANCE.clearAllEffects(true);
        }
    }

    @SubscribeEvent
    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            if (entity == null) {
                entity = Minecraft.getInstance().player;
            }
            if (entity != null) {
                INSTANCE.renderParticles(event.getPoseStack(), event.getCamera(), event.getFrustum(),
                        event.getPartialTick());
            }
        }
    }

    @SubscribeEvent
    public static void debugOverlay(CustomizeGuiOverlayEvent.DebugText event) {
        if (event.getLeft().size() >= 5) {
            String particleTxt = event.getLeft().get(4);
            particleTxt += "." + ChatFormatting.GOLD +
                    " PARTICLE-BACK: " + count(INSTANCE.depthEnabledParticles) +
                    "PARTICLE-FRONT: " + count(INSTANCE.depthDisabledParticles);
            event.getLeft().set(4, particleTxt);
        }
    }

    private static int count(Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> renderQueue) {
        int total = 0;
        for (Deque<GTParticle> queue : renderQueue.values()) {
            total += queue.size();
        }
        return total;
    }
}
