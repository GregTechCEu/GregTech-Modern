package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.particle.GTParticle;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.function.*;

import static com.gregtechceu.gtceu.client.bloom.BloomRenderer.BLOOM_RENDER_LOCK;

@UtilityClass
public class BloomHandler {

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    @UtilityClass
    public static class RenderStage {

        public static RenderLevelStageEvent.@UnknownNullability Stage AFTER_BLOOM;

        @SubscribeEvent
        public static void registerLevelRenderStages(RenderLevelStageEvent.RegisterStageEvent event) {
            AFTER_BLOOM = event.register(GTCEu.id("after_bloom"), GTRenderTypes.bloom());
        }
    }

    static final Map<@Nullable IRenderSetup, BloomRenderList> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    /**
     * Register a custom bloom render callback for subsequent world render. The render call persists until the
     * {@code blockEntity} is invalidated, or the world associated with {@code blockEntity} or the ticket is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}.
     *
     * <p>
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
     * returned instead.
     *
     * @param setup       Render setup, if exists
     * @param render      Rendering callback
     * @param blockEntity Meta tile entity instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || blockEntity == null}
     */
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup, IBloomEffect render,
                                                        BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "blockEntity == null");
        return registerBloomRender(setup,
                new IBloomEffect() {

                    @Override
                    public void renderBloomEffect(PoseStack poseStack, BufferBuilder buffer,
                                                  EffectRenderContext context) {
                        render.renderBloomEffect(poseStack, buffer, context);
                    }

                    @Override
                    public boolean shouldRenderBloomEffect(EffectRenderContext context) {
                        return blockEntity.getLevel() == context.getRenderViewEntity().level() &&
                                render.shouldRenderBloomEffect(context);
                    }
                },
                t -> !blockEntity.isRemoved(),
                blockEntity::getLevel);
    }

    /**
     * Register a custom bloom render callback for subsequent world render. The render call persists until the
     * {@code particle} is invalidated, or the ticket is manually freed by calling
     * {@link BloomRenderTicket#invalidate()}.
     *
     * <p>
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
     * returned instead.
     *
     * @param setup    Render setup, if exists
     * @param render   Rendering callback
     * @param particle Particle instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || metaTileEntity == null}
     */
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup, IBloomEffect render,
                                                        GTParticle particle) {
        Objects.requireNonNull(particle, "particle == null");
        return registerBloomRender(setup, render, t -> particle.isAlive());
    }

    /**
     * Register a custom bloom render callback for subsequent world render. The render call persists until it is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}, or invalidated by validity checker.
     *
     * <p>
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
     * returned instead.
     *
     * @param setup           Render setup, if exists
     * @param render          Rendering callback
     * @param validityChecker Optional validity checker; returning {@code false} causes the ticket to be invalidated.
     *                        Checked on both pre- / post-render each frame.
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, BlockEntity)
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, GTParticle)
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, Predicate, Supplier)
     */
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup, IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker) {
        return registerBloomRender(setup, render, validityChecker, null);
    }

    /**
     * Register a custom bloom render callback for subsequent world render. The render call persists until it is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}, or invalidated by validity checker.
     *
     * <p>
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
     * returned instead.
     *
     * @param setup           Render setup, if exists
     * @param render          Rendering callback
     * @param validityChecker Optional validity checker; returning {@code false} causes the ticket to be invalidated.
     *                        Checked on both pre- / post-render each frame.
     * @param worldContext    Optional world bound to the ticket. If the world returned is not null, the bloom ticket
     *                        will be automatically invalidated on world unload. If world context returns {@code null},
     *                        it will not be affected by aforementioned automatic invalidation.
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, BlockEntity)
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, GTParticle)
     */
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup, IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker,
                                                        @Nullable Supplier<@Nullable Level> worldContext) {
        if (!BloomShaderManager.isBloomActive()) return BloomRenderTicket.INVALID;

        BloomRenderTicket ticket = new BloomRenderTicket(setup, render, validityChecker, worldContext);
        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            SCHEDULED_BLOOM_RENDERS.add(ticket);
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
        return ticket;
    }

    /**
     * Invalidate tickets associated with given level.
     *
     * @param level the level that was unloaded
     */
    static void invalidateLevelData(LevelAccessor level) {
        Objects.requireNonNull(level, "level == null");
        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            for (BloomRenderTicket ticket : BloomHandler.SCHEDULED_BLOOM_RENDERS) {
                if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == level) {
                    ticket.invalidate();
                }
            }

            for (BloomRenderList list : BloomHandler.BLOOM_RENDERS.values()) {
                for (BloomRenderTicket ticket : list) {
                    if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == level) {
                        ticket.invalidate();
                    }
                }
            }
        } finally {
            BLOOM_RENDER_LOCK.readLock().unlock();
        }
    }

    // region internals

    static void initializeScheduledRenders() {
        for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
            if (!ticket.isValid()) continue;
            BloomHandler.BLOOM_RENDERS.computeIfAbsent(ticket.renderSetup, BloomHandler.BloomRenderList::new)
                    .add(ticket);
        }
        SCHEDULED_BLOOM_RENDERS.clear();
    }

    static void removeInvalidatedRenders() {
        BloomHandler.BLOOM_RENDERS.values().removeIf(BloomHandler.BloomRenderList::postDraw);
    }

    static final class BloomRenderList extends ArrayList<BloomRenderTicket> {

        private final @Nullable IRenderSetup renderSetup;

        BloomRenderList(@Nullable IRenderSetup renderSetup) {
            super(2);
            this.renderSetup = renderSetup;
        }

        void draw(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context) {
            boolean initialized = false;

            poseStack.pushPose();
            poseStack.translate(-context.camPos().x(), -context.camPos().y(), -context.camPos().z());

            for (BloomRenderTicket ticket : this) {
                ticket.checkValidity();
                if (!ticket.isValid() || !ticket.render.shouldRenderBloomEffect(context)) continue;

                if (!initialized) {
                    initialized = true;
                    if (this.renderSetup != null) {
                        this.renderSetup.preDraw(buffer);
                    }
                }

                poseStack.pushPose();
                ticket.render.renderBloomEffect(poseStack, buffer, context);
                poseStack.popPose();
            }

            poseStack.popPose();

            if (initialized && this.renderSetup != null) {
                this.renderSetup.postDraw(buffer);
            }
        }

        /**
         * Do post-draw cleanup such as removing invalidated draw tickets.
         *
         * @return Whether this list should be removed from the tracking map.
         */
        boolean postDraw() {
            if (this.isEmpty()) return true;

            boolean removedAny = this.removeIf(ticket -> {
                ticket.checkValidity();
                return !ticket.isValid();
            });
            if (!removedAny) return false;

            return this.isEmpty();
        }
    }

    // endregion
}
