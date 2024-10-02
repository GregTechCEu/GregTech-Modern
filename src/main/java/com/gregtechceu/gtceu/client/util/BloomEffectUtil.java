package com.gregtechceu.gtceu.client.util;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.particle.GTParticle;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.IRenderSetup;
import com.gregtechceu.gtceu.client.shader.PostTarget;
import com.gregtechceu.gtceu.client.shader.Shaders;
import com.gregtechceu.gtceu.client.shader.post.BloomEffect;
import com.gregtechceu.gtceu.client.shader.post.BloomType;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.RenderTypeAccessor;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class BloomEffectUtil {

    private static final Map<BloomRenderKey, List<BloomRenderTicket>> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    private static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    private static final ReentrantLock BLOOM_RENDER_LOCK = new ReentrantLock();

    private static RenderType bloom;
    @Getter
    private static PostTarget bloomFBO;

    /**
     * @return {@link RenderType} instance for the bloom render layer.
     */
    @NotNull
    public static RenderType getBloomLayer() {
        return Objects.requireNonNull(bloom, "Bloom effect is not initialized yet");
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, {@link RenderType#cutout()} is returned instead.
     *
     * @return {@link RenderType} instance for the bloom render layer, or {@link RenderType#cutout()} if bloom
     *         layer is disabled
     * @see #getEffectiveBloomLayer(RenderType)
     */
    @NotNull
    public static RenderType getEffectiveBloomLayer() {
        return getEffectiveBloomLayer(RenderType.cutout());
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, the fallback layer specified is returned instead.
     *
     * @param fallback Block render layer to be returned when bloom layer is disabled
     * @return {@link RenderType} instance for the bloom render layer, or {@code fallback} if bloom layer is
     *         disabled
     * @see #getEffectiveBloomLayer(boolean, RenderType)
     */
    @Contract("null -> _; !null -> !null")
    public static RenderType getEffectiveBloomLayer(RenderType fallback) {
        return GTCEu.isOptifineLoaded() ? fallback : bloom;
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, {@link RenderType#CUTOUT} is returned instead.
     *
     * @param isBloomActive Whether bloom layer should be active. If this value is {@code false}, {@code fallback} layer
     *                      will be returned. Has no effect if Optifine is present.
     * @return {@link RenderType} instance for the bloom render layer, or {@link RenderType#CUTOUT} if bloom
     *         layer is disabled
     * @see #getEffectiveBloomLayer(boolean, RenderType)
     */
    @NotNull
    public static RenderType getEffectiveBloomLayer(boolean isBloomActive) {
        return getEffectiveBloomLayer(isBloomActive, RenderType.cutout());
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Optifine. If the actual bloom layer is
     * disabled, the fallback layer specified is returned instead.
     *
     * @param isBloomActive Whether bloom layer should be active. If this value is {@code false}, {@code fallback} layer
     *                      will be returned. Has no effect if Optifine is present.
     * @param fallback      Block render layer to be returned when bloom layer is disabled
     * @return {@link RenderType} instance for the bloom render layer, or {@code fallback} if bloom layer is
     *         disabled
     */
    @Contract("_, null -> _; _, !null -> !null")
    public static RenderType getEffectiveBloomLayer(boolean isBloomActive, RenderType fallback) {
        return GTCEu.isOptifineLoaded() || !isBloomActive ? fallback : bloom;
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until the
     * {@code blockEntity} is invalidated, or the world associated with {@code blockEntity} or the ticket is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup          Render setup, if exists
     * @param bloomType      Type of the bloom
     * @param render         Rendering callback
     * @param blockEntity Meta tile entity instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || blockEntity == null}
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @NotNull BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "blockEntity == null");
        return registerBloomRender(setup, bloomType,
                new IBloomEffect() {

                    @Override
                    public void renderBloomEffect(@NotNull PoseStack poseStack, @NotNull BufferBuilder buffer, @NotNull EffectRenderContext context) {
                        render.renderBloomEffect(poseStack, buffer, context);
                    }

                    @Override
                    public boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
                        return blockEntity.getLevel() == context.renderViewEntity().level() &&
                                render.shouldRenderBloomEffect(context);
                    }
                },
                t -> !blockEntity.isRemoved(),
                blockEntity::getLevel);
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until the
     * {@code particle} is invalidated, or the ticket is manually freed by calling
     * {@link BloomRenderTicket#invalidate()}.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup     Render setup, if exists
     * @param bloomType Type of the bloom
     * @param render    Rendering callback
     * @param particle  Particle instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || metaTileEntity == null}
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @NotNull GTParticle particle) {
        Objects.requireNonNull(particle, "particle == null");
        return registerBloomRender(setup, bloomType, render, t -> particle.isAlive());
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until it is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}, or invalidated by validity checker.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup           Render setup, if exists
     * @param bloomType       Type of the bloom
     * @param render          Rendering callback
     * @param validityChecker Optional validity checker; returning {@code false} causes the ticket to be invalidated.
     *                        Checked on both pre-/post-render each frame.
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, BlockEntity)
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, GTParticle)
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, Predicate, Supplier)
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker) {
        return registerBloomRender(setup, bloomType, render, validityChecker, null);
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until it is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}, or invalidated by validity checker.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Optifine is present, and an invalid ticket will be
     * returned instead.
     * </p>
     *
     * @param setup           Render setup, if exists
     * @param bloomType       Type of the bloom
     * @param render          Rendering callback
     * @param validityChecker Optional validity checker; returning {@code false} causes the ticket to be invalidated.
     *                        Checked on both pre/post render each frame.
     * @param worldContext    Optional world bound to the ticket. If the world returned is not null, the bloom ticket
     *                        will be automatically invalidated on world unload. If world context returns {@code null},
     *                        it will not be affected by aforementioned automatic invalidation.
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, BlockEntity)
     * @see #registerBloomRender(IRenderSetup, BloomType, IBloomEffect, GTParticle)
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull BloomType bloomType,
                                                        @NotNull IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker,
                                                        @Nullable Supplier<Level> worldContext) {
        if (GTCEu.isOptifineLoaded()) return BloomRenderTicket.INVALID;
        BloomRenderTicket ticket = new BloomRenderTicket(setup, bloomType, render, validityChecker, worldContext);
        BLOOM_RENDER_LOCK.lock();
        try {
            SCHEDULED_BLOOM_RENDERS.add(ticket);
        } finally {
            BLOOM_RENDER_LOCK.unlock();
        }
        return ticket;
    }

    /**
     * Invalidate tickets associated with given world.
     *
     * @param world Level
     */
    public static void invalidateLevelTickets(@NotNull Level world) {
        Objects.requireNonNull(world, "world == null");
        BLOOM_RENDER_LOCK.lock();
        try {
            for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
                if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == world) {
                    ticket.invalidate();
                }
            }

            for (Map.Entry<BloomRenderKey, List<BloomRenderTicket>> e : BLOOM_RENDERS.entrySet()) {
                for (BloomRenderTicket ticket : e.getValue()) {
                    if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == world) {
                        ticket.invalidate();
                    }
                }
            }
        } finally {
            BLOOM_RENDER_LOCK.unlock();
        }
    }

    public static void init() {
        bloom = GTRenderTypes.getBloom();
        ((RenderTypeAccessor)bloom).setChunkLayerId(RenderType.chunkBufferLayers().size());
        RenderType.CHUNK_BUFFER_LAYERS = ImmutableList.<RenderType>builder()
                .addAll(RenderType.CHUNK_BUFFER_LAYERS)
                .add(bloom)
                .build();

        if (GTCEu.isSodiumRubidiumEmbeddiumLoaded()) {
            try {
                // Add our render type to embeddium's render passes by force (until an API is added)

                /* FOR UNRELEASED EMBEDDIUM!!!
                Field field = DefaultTerrainRenderPasses.class.getDeclaredField("RENDER_PASS_MAPPINGS");
                FieldUtils.removeFinalModifier(field);
                field.set(null, new HashMap<>(DefaultTerrainRenderPasses.RENDER_PASS_MAPPINGS));

                TerrainRenderPass bloomPass = TerrainRenderPass.builder()
                        .layer(bloom)
                        .fragmentDiscard(true)
                        .useReverseOrder(true)
                        .useTranslucencySorting(true)
                        .build();
                DefaultTerrainRenderPasses.RENDER_PASS_MAPPINGS.put(bloom, List.of(bloomPass));
                */
                TerrainRenderPass bloomPass = new TerrainRenderPass(bloom, true, true);

                Field field = DefaultTerrainRenderPasses.class.getDeclaredField("ALL");
                FieldUtils.removeFinalModifier(field);
                field.set(null, ArrayUtils.add(DefaultTerrainRenderPasses.ALL, bloomPass));
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static final ThreadLocal<Boolean> isRenderingBloom = ThreadLocal.withInitial(() -> false);

    public static void renderBloomBlockLayer(LevelRenderer levelRenderer,
                                             double camX, double camY, double camZ,
                                             PoseStack poseStack,
                                             Camera camera,
                                             Frustum frustum,
                                             RenderType blockRenderLayer, // 70% sure it's translucent uh yeah
                                             double partialTicks,
                                             Matrix4f projectionMatrix,
                                             @NotNull Entity entity) {
        Minecraft.getInstance().getProfiler().popPush("BTLayer");
        isRenderingBloom.set(true);

        if (GTCEu.isOptifineLoaded()) {
            levelRenderer.renderChunkLayer(blockRenderLayer, poseStack, camX, camY, camZ, projectionMatrix);
            return;
        }

        BLOOM_RENDER_LOCK.lock();
        try {
            renderBloomInternal(levelRenderer, camX, camY, camZ, poseStack, camera, frustum, blockRenderLayer, partialTicks, projectionMatrix, entity);
        } finally {
            BLOOM_RENDER_LOCK.unlock();
            isRenderingBloom.set(false);
        }
    }

    private static void renderBloomInternal(LevelRenderer levelRenderer,
                                           double camX, double camY, double camZ,
                                           PoseStack poseStack,
                                           Camera camera,
                                           Frustum frustum,
                                           RenderType blockRenderLayer,
                                           double partialTicks,
                                           Matrix4f projectionMatrix,
                                           @NotNull Entity entity) {
        preDraw();

        EffectRenderContext context = EffectRenderContext.getInstance().update(entity, camera, frustum, (float) partialTicks);

        if (!ConfigHolder.INSTANCE.client.shader.emissiveTexturesBloom) {
            RenderSystem.depthMask(true);
            levelRenderer.renderChunkLayer(bloom, poseStack, camX, camY, camZ, projectionMatrix);

            if (!BLOOM_RENDERS.isEmpty()) {
                BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                for (List<BloomRenderTicket> list : BLOOM_RENDERS.values()) {
                    draw(poseStack, buffer, context, list);
                }
            }
            postDraw();
            RenderSystem.depthMask(false);

            levelRenderer.renderChunkLayer(blockRenderLayer, poseStack, camX, camY, camZ, projectionMatrix);
            return;
        }

        RenderTarget fbo = Minecraft.getInstance().getMainRenderTarget();

        if (bloomFBO == null ||
                bloomFBO.width != fbo.width ||
                bloomFBO.height != fbo.height ||
                (fbo.isStencilEnabled() && !bloomFBO.isStencilEnabled())) {
            if (bloomFBO == null) {
                bloomFBO = new PostTarget(fbo.width, fbo.height, false);
                bloomFBO.setClearColor(0, 0, 0, 0);
            } else {
                bloomFBO.resize(fbo.width, fbo.height, Minecraft.ON_OSX);
            }

            if (fbo.isStencilEnabled() && !bloomFBO.isStencilEnabled()) {
                bloomFBO.enableStencil();
            }

            if (DepthTextureUtil.isLastBind() && DepthTextureUtil.isUseDefaultFBO()) {
                RenderUtil.hookDepthTexture(bloomFBO, DepthTextureUtil.framebufferDepthTexture);
            } else {
                RenderUtil.hookDepthBuffer(bloomFBO, fbo.getDepthTextureId());
            }

            bloomFBO.setFilterMode(GL11.GL_LINEAR);
        }

        RenderSystem.depthMask(true);
        fbo.bindWrite(true);

        if (!BLOOM_RENDERS.isEmpty()) {
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            for (List<BloomRenderTicket> list : BLOOM_RENDERS.values()) {
                draw(poseStack, buffer, context, list);
            }
        }

        // render to BLOOM BUFFER
        bloomFBO.clear(Minecraft.ON_OSX);
        bloomFBO.bindWrite(false);

        levelRenderer.renderChunkLayer(bloom, poseStack, camX, camY, camZ, projectionMatrix);

        RenderSystem.depthMask(false);

        // fast render bloom layer to main fbo
        bloomFBO.bindRead();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE, null);

        // reset transparent layer render state and render
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo.frameBufferId);
        RenderSystem.enableBlend();
        Minecraft.getInstance().getTextureManager().bindForSetup(InventoryMenu.BLOCK_ATLAS);
        //GL11.glShadeModel(GL11.GL_SMOOTH);

        levelRenderer.renderChunkLayer(blockRenderLayer, poseStack, camX, camY, camZ, projectionMatrix);

        Minecraft.getInstance().getProfiler().popPush("bloom");

        // blend bloom + transparent
        fbo.bindRead();
        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
        Shaders.renderFullImageInFBO(bloomFBO, Shaders.IMAGE, null);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        // render bloom effect to fbo
        BloomEffect.strength = ConfigHolder.INSTANCE.client.shader.strength;
        BloomEffect.baseBrightness = ConfigHolder.INSTANCE.client.shader.baseBrightness;
        BloomEffect.highBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.highBrightnessThreshold;
        BloomEffect.lowBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.lowBrightnessThreshold;
        BloomEffect.step = ConfigHolder.INSTANCE.client.shader.step;
        switch (ConfigHolder.INSTANCE.client.shader.bloomStyle) {
            case 0 -> BloomEffect.renderLOG(bloomFBO, fbo);
            case 1 -> BloomEffect.renderUnity(bloomFBO, fbo);
            case 2 -> BloomEffect.renderUnreal(bloomFBO, fbo);
            default -> {
                postDraw();
                RenderSystem.depthMask(false);
                RenderSystem.disableBlend();
                return;
            }
        }

        RenderSystem.depthMask(false);

        // render bloom blend result to fbo
        RenderSystem.disableBlend();
        Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE, null);

        // ********** render custom bloom ************

        if (!BLOOM_RENDERS.isEmpty()) {
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            for (var e : BLOOM_RENDERS.entrySet()) {
                BloomRenderKey key = e.getKey();
                List<BloomRenderTicket> list = e.getValue();

                RenderSystem.depthMask(true);

                bloomFBO.clear(Minecraft.ON_OSX);
                bloomFBO.bindWrite(true);

                draw(poseStack, buffer, context, list);

                RenderSystem.depthMask(false);

                // blend bloom + transparent
                fbo.bindRead();
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
                Shaders.renderFullImageInFBO(bloomFBO, Shaders.IMAGE, null);
                RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                switch (key.bloomType) {
                    case GAUSSIAN -> BloomEffect.renderLOG(bloomFBO, fbo);
                    case UNITY -> BloomEffect.renderUnity(bloomFBO, fbo);
                    case UNREAL -> BloomEffect.renderUnreal(bloomFBO, fbo);
                    default -> {
                        RenderSystem.disableBlend();
                        continue;
                    }
                }

                // render bloom blend result to fbo
                RenderSystem.disableBlend();
                Shaders.renderFullImageInFBO(fbo, Shaders.IMAGE, null);
            }
            postDraw();
        }
    }

    private static void preDraw() {
        for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
            if (!ticket.isValid()) continue;
            BLOOM_RENDERS.computeIfAbsent(new BloomRenderKey(ticket.renderSetup, ticket.bloomType),
                    k -> new ArrayList<>()).add(ticket);
        }
        SCHEDULED_BLOOM_RENDERS.clear();
    }

    private static void draw(@NotNull PoseStack poseStack,
                             @NotNull BufferBuilder buffer, @NotNull EffectRenderContext context,
                             @NotNull List<BloomRenderTicket> tickets) {
        boolean initialized = false;
        @Nullable
        IRenderSetup renderSetup = null;
        for (BloomRenderTicket ticket : tickets) {
            ticket.checkValidity();
            if (!ticket.isValid() || !ticket.render.shouldRenderBloomEffect(context)) continue;
            if (!initialized) {
                initialized = true;
                renderSetup = ticket.renderSetup;
                if (renderSetup != null) {
                    renderSetup.preDraw(buffer);
                }
            }
            ticket.render.renderBloomEffect(poseStack, buffer, context);
        }
        if (initialized && renderSetup != null) {
            renderSetup.postDraw(buffer);
        }
    }

    private static void postDraw() {
        for (var it = BLOOM_RENDERS.values().iterator(); it.hasNext();) {
            List<BloomRenderTicket> list = it.next();

            if (!list.isEmpty()) {
                if (!list.removeIf(ticket -> {
                    ticket.checkValidity();
                    return !ticket.isValid();
                }) || !list.isEmpty()) continue;
            }

            it.remove();
        }
    }

    private record BloomRenderKey(@Nullable IRenderSetup renderSetup, @NotNull BloomType bloomType) {}

    public static final class BloomRenderTicket {

        public static final BloomRenderTicket INVALID = new BloomRenderTicket();

        @Nullable
        private final IRenderSetup renderSetup;
        private final BloomType bloomType;
        private final IBloomEffect render;
        @Nullable
        private final Predicate<BloomRenderTicket> validityChecker;
        @Nullable
        private final Supplier<Level> worldContext;

        private boolean invalidated;

        BloomRenderTicket() {
            this(null, BloomType.DISABLED, (p, b, c) -> {}, null, null);
            this.invalidated = true;
        }

        BloomRenderTicket(@Nullable IRenderSetup renderSetup, @NotNull BloomType bloomType,
                          @NotNull IBloomEffect render, @Nullable Predicate<BloomRenderTicket> validityChecker,
                          @Nullable Supplier<Level> worldContext) {
            this.renderSetup = renderSetup;
            this.bloomType = Objects.requireNonNull(bloomType, "bloomType == null");
            this.render = Objects.requireNonNull(render, "render == null");
            this.validityChecker = validityChecker;
            this.worldContext = worldContext;
        }

        @Nullable
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
        public IRenderSetup getRenderSetup() {
            return this.renderSetup;
        }

        @NotNull
        @Deprecated
        @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
        public BloomType getBloomType() {
            return this.bloomType;
        }

        public boolean isValid() {
            return !this.invalidated;
        }

        public void invalidate() {
            this.invalidated = true;
        }

        private void checkValidity() {
            if (!this.invalidated && this.validityChecker != null && !this.validityChecker.test(this)) {
                invalidate();
            }
        }
    }

    /**
     * @deprecated use ticket-based bloom render hooks
     */
    @Deprecated
    @ApiStatus.ScheduledForRemoval(inVersion = "2.9")
    public interface IBloomRenderFast extends IRenderSetup {

        /**
         * Custom Bloom Style.
         *
         * @return 0 - Simple Gaussian Blur Bloom
         *         <p>
         *         1 - Unity Bloom
         *         </p>
         *         <p>
         *         2 - Unreal Bloom
         *         </p>
         */
        int customBloomStyle();
    }
}
