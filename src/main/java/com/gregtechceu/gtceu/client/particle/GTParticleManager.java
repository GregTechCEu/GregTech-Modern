package com.gregtechceu.gtceu.client.particle;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.bloom.EffectRenderContext;
import com.gregtechceu.gtceu.client.bloom.IRenderSetup;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Singleton class responsible for managing, updating and rendering {@link GTParticle} instances.
 */
public final class GTParticleManager {

    public static final GTParticleManager INSTANCE = new GTParticleManager();

    private final Map<@Nullable IRenderSetup, Queue<GTParticle>> depthEnabledParticles = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<@Nullable IRenderSetup, Queue<GTParticle>> depthDisabledParticles = new Object2ObjectLinkedOpenHashMap<>();

    private final List<GTParticle> newParticleQueue = new ArrayList<>();

    private GTParticleManager() {}

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

                Queue<GTParticle> particles = queue.computeIfAbsent(particle.getRenderSetup(),
                        setup -> new ArrayDeque<>());

                if (particles.size() > 6000) {
                    particles.remove().setExpired();
                }
                particles.add(particle);
            }

            newParticleQueue.clear();
        }
    }

    private void updateQueue(Map<@Nullable IRenderSetup, Queue<GTParticle>> renderQueue) {
        Iterator<Queue<GTParticle>> it = renderQueue.values().iterator();
        while (it.hasNext()) {
            Queue<GTParticle> particlesForSetup = it.next();

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
        for (Queue<GTParticle> particles : this.depthEnabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        for (Queue<GTParticle> particles : this.depthDisabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        this.depthEnabledParticles.clear();
        this.depthDisabledParticles.clear();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void renderParticles(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        if (this.depthEnabledParticles.isEmpty() && this.depthDisabledParticles.isEmpty()) return;

        Camera camera = event.getCamera();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);

        EffectRenderContext instance = EffectRenderContext.getInstance()
                .update(camera, event.getFrustum(), event.getPartialTick().getGameTimeDeltaPartialTick(false));

        if (!this.depthDisabledParticles.isEmpty()) {
            RenderSystem.depthMask(false);
            renderParticlesInLayer(poseStack, this.depthDisabledParticles, instance);
            RenderSystem.depthMask(true);
        }
        renderParticlesInLayer(poseStack, this.depthEnabledParticles, instance);

        poseStack.popPose();
    }

    private static void renderParticlesInLayer(PoseStack poseStack,
                                               Map<@Nullable IRenderSetup, Queue<GTParticle>> renderQueue,
                                               EffectRenderContext context) {
        for (var entry : renderQueue.entrySet()) {
            Queue<GTParticle> particles = entry.getValue();
            if (particles.isEmpty()) continue;

            IRenderSetup handler = entry.getKey();
            boolean initialized = false;
            BufferBuilder buffer = null;

            for (GTParticle particle : particles) {
                if (!particle.shouldRender(context)) {
                    continue;
                }
                try {
                    if (!initialized) {
                        initialized = true;
                        if (handler != null) {
                            buffer = handler.preDraw();
                        }
                        if (buffer == null) {
                            buffer = Tesselator.getInstance()
                                    .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
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
    public void clientTick(ClientTickEvent.Post event) {
        if (Minecraft.getInstance().isPaused()) {
            return;
        }
        if (Minecraft.getInstance().level != null) {
            INSTANCE.updateEffects();
        }
    }

    @SubscribeEvent
    public void onClientLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ClientLevel newLevel)) {
            return;
        }
        ClientLevel oldLevel = Minecraft.getInstance().level;
        if (oldLevel != newLevel) {
            this.clearAllEffects(oldLevel != null);
        }
    }

    @SubscribeEvent
    public void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (event.getPlayer() != null) {
            this.clearAllEffects(true);
        }
    }

    @SubscribeEvent
    public void debugOverlay(CustomizeGuiOverlayEvent.DebugText event) {
        List<String> gameInfo = event.getLeft();
        if (gameInfo.size() >= 5) {
            String countStatsLine = gameInfo.get(4);
            countStatsLine += ". " + ChatFormatting.GOLD +
                    "P-BACK: " + count(this.depthEnabledParticles) +
                    " P-FRONT: " + count(this.depthDisabledParticles);
            gameInfo.set(4, countStatsLine);
        }
    }

    private static int count(Map<@Nullable IRenderSetup, Queue<GTParticle>> renderQueue) {
        return renderQueue.values().stream().mapToInt(Queue::size).sum();
    }
}
