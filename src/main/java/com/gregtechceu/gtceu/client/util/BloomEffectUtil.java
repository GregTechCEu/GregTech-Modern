package com.gregtechceu.gtceu.client.util;

import com.google.common.collect.ImmutableList;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.particle.GTParticle;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.renderer.IRenderSetup;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.client.shader.post.BloomEffect;
import com.gregtechceu.gtceu.client.shader.post.BloomType;

import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.PostChainAccessor;
import com.gregtechceu.gtceu.core.mixins.RenderTypeAccessor;
import com.gregtechceu.gtceu.core.mixins.embeddium.DefaultTerrainRenderPassesAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.DefaultTerrainRenderPasses;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.TerrainRenderPass;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material;
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.parameters.AlphaCutoffParameter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.EffectInstance;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

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
    /**
     * @return {@link RenderType} instance for the bloom render layer.
     */
    @NotNull
    public static RenderType getBloomLayer() {
        return Objects.requireNonNull(bloom, "Bloom effect is not initialized yet");
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Iris/Oculus. If the actual bloom layer is
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
     * layers can be changed depending on external factors, such as presence of Iris/Oculus. If the actual bloom layer is
     * disabled, the fallback layer specified is returned instead.
     *
     * @param fallback Block render layer to be returned when bloom layer is disabled
     * @return {@link RenderType} instance for the bloom render layer, or {@code fallback} if bloom layer is
     *         disabled
     * @see #getEffectiveBloomLayer(boolean, RenderType)
     */
    @Contract("null -> _; !null -> !null")
    public static RenderType getEffectiveBloomLayer(RenderType fallback) {
        return GTCEu.isIrisOculusLoaded() ? fallback : bloom;
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Iris/Oculus. If the actual bloom layer is
     * disabled, {@link RenderType#cutout()} is returned instead.
     *
     * @param isBloomActive Whether bloom layer should be active. If this value is {@code false}, {@code fallback} layer
     *                      will be returned. Has no effect if Iris/Oculus is present.
     * @return {@link RenderType} instance for the bloom render layer, or {@link RenderType#cutout()} if bloom
     *         layer is disabled
     * @see #getEffectiveBloomLayer(boolean, RenderType)
     */
    @NotNull
    public static RenderType getEffectiveBloomLayer(boolean isBloomActive) {
        return getEffectiveBloomLayer(isBloomActive, RenderType.cutout());
    }

    /**
     * Get "effective bloom layer", i.e. the actual render layer that emissive textures get rendered. Effective bloom
     * layers can be changed depending on external factors, such as presence of Iris/Oculus. If the actual bloom layer is
     * disabled, the fallback layer specified is returned instead.
     *
     * @param isBloomActive Whether bloom layer should be active. If this value is {@code false}, {@code fallback} layer
     *                      will be returned. Has no effect if Iris/Oculus is present.
     * @param fallback      Block render layer to be returned when bloom layer is disabled
     * @return {@link RenderType} instance for the bloom render layer, or {@code fallback} if bloom layer is
     *         disabled
     */
    @Contract("_, null -> _; _, !null -> !null")
    public static RenderType getEffectiveBloomLayer(boolean isBloomActive, RenderType fallback) {
        return GTCEu.isIrisOculusLoaded() || !isBloomActive ? fallback : bloom;
    }

    /**
     * <p>
     * Register a custom bloom render callback for subsequent world render. The render call persists until the
     * {@code blockEntity} is invalidated, or the world associated with {@code blockEntity} or the ticket is
     * manually freed by calling {@link BloomRenderTicket#invalidate()}.
     * </p>
     * <p>
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
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
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
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
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
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
     * This method does not register bloom render ticket when Iris/Oculus is present, and an invalid ticket will be
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
        if (GTCEu.isIrisOculusLoaded()) return BloomRenderTicket.INVALID;
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
     * Invalidate tickets associated with given level.
     *
     * @param level the level that was unloaded
     */
    public static void invalidateLevelTickets(@NotNull LevelAccessor level) {
        Objects.requireNonNull(level, "level == null");
        BLOOM_RENDER_LOCK.lock();
        try {
            for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
                if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == level) {
                    ticket.invalidate();
                }
            }

            for (Map.Entry<BloomRenderKey, List<BloomRenderTicket>> e : BLOOM_RENDERS.entrySet()) {
                for (BloomRenderTicket ticket : e.getValue()) {
                    if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == level) {
                        ticket.invalidate();
                    }
                }
            }
        } finally {
            BLOOM_RENDER_LOCK.unlock();
        }
    }

    public static Supplier<Supplier<Material>> EMBEDDIUM_MATERIAL_BLOOM;

    public static void init() {
        bloom = GTRenderTypes.getBloom();
        ((RenderTypeAccessor)bloom).setChunkLayerId(RenderType.chunkBufferLayers().size());
        RenderType.CHUNK_BUFFER_LAYERS = ImmutableList.<RenderType>builder()
                .addAll(RenderType.CHUNK_BUFFER_LAYERS)
                .add(bloom)
                .build();

        if (GTCEu.isSodiumRubidiumEmbeddiumLoaded()) {
            // Add our render type to embeddium's render passes by force (until an API is added)

            /* FOR UNRELEASED EMBEDDIUM!!!

            TerrainRenderPass bloomPass = TerrainRenderPass.builder()
                    .layer(bloom)
                    .fragmentDiscard(true)
                    .useReverseOrder(false)
                    .useTranslucencySorting(true)
                    .build();
            var map = new HashMap<>(DefaultTerrainRenderPassesAccessor.getRenderPassMappings());
            map.put(bloom, List.of(bloomPass));
            DefaultTerrainRenderPassesAccessor.setRenderPassMappings(map);
            */
            TerrainRenderPass bloomPass = new TerrainRenderPass(bloom, false, true);
            DefaultTerrainRenderPassesAccessor.setAll(ArrayUtils.add(DefaultTerrainRenderPasses.ALL, bloomPass));

            Material bloomMaterial = new Material(bloomPass, AlphaCutoffParameter.ZERO, true);
            EMBEDDIUM_MATERIAL_BLOOM = () -> () -> bloomMaterial;
        }
    }

    public static void renderBloom(double camX, double camY, double camZ,
                                   PoseStack poseStack,
                                   Frustum frustum,
                                   double partialTicks,
                                   @NotNull Entity entity) {
        Minecraft.getInstance().getProfiler().popPush("BTLayer");

        preDraw();

        EffectRenderContext context = EffectRenderContext.getInstance()
                .update(entity, camX, camY, camZ, frustum, (float) partialTicks);

        if (!ConfigHolder.INSTANCE.client.shader.emissiveTexturesBloom) {
            RenderSystem.depthMask(true);

            if (!BLOOM_RENDERS.isEmpty()) {
                BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                for (List<BloomRenderTicket> list : BLOOM_RENDERS.values()) {
                    draw(poseStack, buffer, context, list);
                }
            }
            postDraw();
            RenderSystem.depthMask(false);

            render();
            return;
        }

        BloomEffect.strength = ConfigHolder.INSTANCE.client.shader.strength;
        BloomEffect.baseBrightness = ConfigHolder.INSTANCE.client.shader.baseBrightness;
        BloomEffect.highBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.highBrightnessThreshold;
        BloomEffect.lowBrightnessThreshold = ConfigHolder.INSTANCE.client.shader.lowBrightnessThreshold;
        BloomEffect.step = ConfigHolder.INSTANCE.client.shader.step;

        // ********** render custom bloom ************

        if (!BLOOM_RENDERS.isEmpty()) {
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            for (var e : BLOOM_RENDERS.entrySet()) {
                BloomRenderKey key = e.getKey();
                List<BloomRenderTicket> list = e.getValue();

                RenderSystem.depthMask(true);
                draw(poseStack, buffer, context, list);
                RenderSystem.depthMask(false);
            }
        }

        RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ZERO);
        RenderSystem.enableBlend();
        render();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableBlend();

        postDraw();
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

    private static final String UNREAL_COMPOSITE_SHADER_NAME = "gtceu:unreal_composite";
    private static final String UNITY_COMPOSITE_SHADER_NAME = "gtceu:unity_composite";
    private static final String SEPERABLE_BLUR_SHADER_NAME = "gtceu:seperable_blur";
    private static final String BLOOM_INTENSIVE_UNIFORM = "BloomIntensive";
    private static final String BLOOM_BASE_UNIFORM = "BloomBase";
    private static final String BLOOM_THRESHOLD_UP_UNIFORM = "BloomThresholdUp";
    private static final String BLOOM_THRESHOLD_DOWN_UNIFORM = "BloomThresholdDown";
    private static final String BLUR_DIR_UNIFORM = "BlurDir";

    private static void render() {
        if (GTShaders.allowedShader()) {
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            // Forcefully insert config values to shader
            List<PostPass> passes = ((PostChainAccessor) GTShaders.BLOOM_CHAIN).getPasses();
            for (PostPass pass : passes) {
                EffectInstance shader = pass.getEffect();
                int index = passes.indexOf(pass);
                if (GTShaders.BLOOM_TYPE == BloomType.UNREAL) {
                    if (shader.getName().equals(SEPERABLE_BLUR_SHADER_NAME)) {
                        switch (index) {
                            case 1, 3, 5, 7 -> shader.safeGetUniform(BLUR_DIR_UNIFORM).set(BloomEffect.step, 0.0f);
                            case 2, 4, 6, 8 -> shader.safeGetUniform(BLUR_DIR_UNIFORM).set(0.0f, BloomEffect.step);
                        }
                    }
                }
                if (shader.getName().equals(UNITY_COMPOSITE_SHADER_NAME) ||
                        shader.getName().equals(UNREAL_COMPOSITE_SHADER_NAME)) {
                    shader.safeGetUniform(BLOOM_INTENSIVE_UNIFORM).set(BloomEffect.strength);
                    shader.safeGetUniform(BLOOM_BASE_UNIFORM).set(BloomEffect.baseBrightness);
                    shader.safeGetUniform(BLOOM_THRESHOLD_UP_UNIFORM).set(BloomEffect.highBrightnessThreshold);
                    shader.safeGetUniform(BLOOM_THRESHOLD_DOWN_UNIFORM).set(BloomEffect.lowBrightnessThreshold);
                }
            }

            GTShaders.BLOOM_TARGET.blitToScreen(GTShaders.mc.getWindow().getWidth(), GTShaders.mc.getWindow().getHeight(), false);
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
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
}
