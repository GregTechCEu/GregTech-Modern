package com.gregtechceu.gtceu.client.particle;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.renderer.IRenderSetup;
import com.gregtechceu.gtceu.client.util.EffectRenderContext;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Singleton class responsible for managing, updating and rendering {@link GTParticle} instances.
 */
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = GTCEu.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GTParticleManager {

    public static final GTParticleManager INSTANCE = new GTParticleManager();

    @Nullable
    private static Level currentWorld = null;

    private final Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> depthEnabledParticles = new Object2ObjectLinkedOpenHashMap<>();
    private final Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> depthDisabledParticles = new Object2ObjectLinkedOpenHashMap<>();

    private final List<GTParticle> newParticleQueue = new ArrayList<>();

    public void addEffect(@NotNull GTParticle particles) {
        newParticleQueue.add(particles);
    }

    public void updateEffects() {
        if (!depthEnabledParticles.isEmpty()) {
            updateQueue(depthEnabledParticles);
        }
        if (!depthDisabledParticles.isEmpty()) {
            updateQueue(depthDisabledParticles);
        }
        if (!newParticleQueue.isEmpty()) {
            for (GTParticle particle : newParticleQueue) {
                var queue = particle.shouldDisableDepth() ? depthDisabledParticles : depthEnabledParticles;

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

    private void updateQueue(Map<IRenderSetup, ArrayDeque<GTParticle>> renderQueue) {
        Iterator<ArrayDeque<GTParticle>> it = renderQueue.values().iterator();
        while (it.hasNext()) {
            ArrayDeque<GTParticle> particles = it.next();

            Iterator<GTParticle> it2 = particles.iterator();
            while (it2.hasNext()) {
                GTParticle particle = it2.next();
                if (particle.isAlive()) {
                    try {
                        particle.onUpdate();
                    } catch (RuntimeException exception) {
                        GTCEu.LOGGER.error("particle update error: {}", particle.toString(), exception);
                        particle.setExpired();
                    }
                    if (particle.isAlive()) continue;
                }
                it2.remove();
            }

            if (particles.isEmpty()) {
                it.remove();
            }
        }
    }

    public void clearAllEffects(boolean cleanNewQueue) {
        if (cleanNewQueue) {
            for (GTParticle particle : newParticleQueue) {
                particle.setExpired();
            }
            newParticleQueue.clear();
        }
        for (ArrayDeque<GTParticle> particles : depthEnabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        for (ArrayDeque<GTParticle> particles : depthDisabledParticles.values()) {
            for (GTParticle particle : particles) {
                particle.setExpired();
            }
        }
        depthEnabledParticles.clear();
        depthDisabledParticles.clear();
    }

    public void renderParticles(@NotNull Entity renderViewEntity, float partialTicks) {
        if (depthEnabledParticles.isEmpty() && depthDisabledParticles.isEmpty()) return;

        EffectRenderContext instance = EffectRenderContext.getInstance().update(renderViewEntity, partialTicks);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

        if (!depthDisabledParticles.isEmpty()) {
            RenderSystem.depthMask(false);

            renderGlParticlesInLayer(depthDisabledParticles, instance);

            RenderSystem.depthMask(true);
        }

        renderGlParticlesInLayer(depthEnabledParticles, instance);

        RenderSystem.disableBlend();
    }

    private static void renderGlParticlesInLayer(@NotNull Map<@Nullable IRenderSetup, ArrayDeque<GTParticle>> renderQueue,
                                                 @NotNull EffectRenderContext context) {
        for (var e : renderQueue.entrySet()) {
            @Nullable
            IRenderSetup handler = e.getKey();
            ArrayDeque<GTParticle> particles = e.getValue();
            if (particles.isEmpty()) continue;

            boolean initialized = false;
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            for (GTParticle particle : particles) {
                if (particle.shouldRender(context)) {
                    try {
                        if (!initialized) {
                            initialized = true;
                            if (handler != null) {
                                handler.preDraw(buffer);
                            }
                        }
                        particle.renderParticle(buffer, context);
                    } catch (Throwable throwable) {
                        GTCEu.LOGGER.error("particle render error: {}", particle, throwable);
                        particle.setExpired();
                    }
                }
            }
            if (initialized && handler != null) {
                handler.postDraw(buffer);
            }
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().isPaused()) {
            return;
        }

        ClientLevel world = Minecraft.getInstance().level;
        if (currentWorld != world) {
            INSTANCE.clearAllEffects(currentWorld != null);
            currentWorld = world;
        }

        if (currentWorld != null) {
            INSTANCE.updateEffects();
        }
    }

    @SubscribeEvent
    public static void renderWorld(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            Entity entity = Minecraft.getInstance().getCameraEntity();
            INSTANCE.renderParticles(entity == null ? Minecraft.getInstance().player : entity, event.getPartialTick());
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
        int g = 0;
        for (Deque<GTParticle> queue : renderQueue.values()) {
            g += queue.size();
        }
        return g;
    }
}
