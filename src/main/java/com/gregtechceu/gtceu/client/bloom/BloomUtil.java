package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.particle.GTParticle;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;
import com.gregtechceu.gtceu.core.mixins.client.RenderStateShardAccessor;
import com.gregtechceu.gtceu.core.mixins.client.bloom.PostChainAccessor;
import com.gregtechceu.gtceu.core.mixins.client.bloom.normal.LevelRendererAccessor;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.function.IntObjectConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.*;

import static com.gregtechceu.gtceu.client.bloom.BloomShaderManager.BLOOM_TARGET;

@UtilityClass
public class BloomUtil {

    public static RenderLevelStageEvent.@UnknownNullability Stage AFTER_BLOOM_RENDER_STAGE;

    static final Map<@Nullable IRenderSetup, BloomRenderList> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    static final ReadWriteLock BLOOM_RENDER_LOCK = new ReentrantReadWriteLock();

    public static void init() {}

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

    @ApiStatus.Internal
    public static void renderBloom(Camera camera, PoseStack poseStack, Frustum frustum, Matrix4f projectionMatrix,
                                   float partialTicks, LevelRenderer levelRenderer, ProfilerFiller profilerFiller) {
        if (!BloomShaderManager.isBloomActive()) return;

        Vec3 camPos = camera.getPosition();

        profilerFiller.popPush("gtceu:bloom");
        BloomUtil.setupBloomShaderUniforms();

        GTRenderTypes.bloom().setupRenderState();

        renderSpecialBloom(camera, poseStack, frustum, partialTicks, profilerFiller);

        // safe mode disabled -> use deeper, faster hackery
        if (!GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SAFE_MODE_CONFIG_NAME)) {
            ((LevelRendererAccessor) levelRenderer).invokeRenderChunkLayer(GTRenderTypes.bloom(), poseStack,
                    camPos.x, camPos.y, camPos.z, projectionMatrix);

            // have to re-setup here. so sad. very aw.
            GTRenderTypes.bloom().setupRenderState();
        }
        // safe mode enabled -> don't draw block bloom the 'normal' way; use BloomSafeMode.drawBlockBloom instead
        else {
            BloomSafeMode.drawBlockBloom(camera, poseStack, frustum, projectionMatrix, levelRenderer, profilerFiller);
        }

        BloomUtil.processPostEffect(partialTicks, profilerFiller);

        // clear state. again.
        GTRenderTypes.bloom().clearRenderState();

        // profiler section is popped by popPush() in the calling function; don't pop it here
    }

    private static void renderSpecialBloom(Camera camera, PoseStack poseStack, Frustum frustum, float partialTicks,
                                           ProfilerFiller profilerFiller) {
        profilerFiller.push("special");

        // render state is set up & cleared in calling function

        preDraw();
        if (!BLOOM_RENDERS.isEmpty()) {
            EffectRenderContext context = EffectRenderContext.getInstance().update(camera, frustum, partialTicks);

            BLOOM_RENDER_LOCK.readLock().lock();
            try {
                BLOOM_RENDERS.forEach((renderSetup, list) -> {
                    BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                    list.draw(poseStack, buffer, context);
                });
            } finally {
                BLOOM_RENDER_LOCK.readLock().unlock();
            }

            postDraw();
        }

        profilerFiller.pop();
    }

    public static void processPostEffect(float partialTicks, ProfilerFiller profilerFiller) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();

        profilerFiller.push("processPostEffect");

        BloomUtil.setFilterToggleUniform(true);
        BloomShaderManager.BLOOM_CHAIN.process(partialTicks);
        BloomUtil.setFilterToggleUniform(false);

        mainTarget.bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA,
                SourceFactor.ZERO, DestFactor.ONE);

        BLOOM_TARGET.blitToScreen(mainTarget.viewWidth, mainTarget.viewHeight, false);
        BLOOM_TARGET.unbindRead();

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        profilerFiller.pop();
    }

    static void preDraw() {
        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
                if (!ticket.isValid()) continue;
                BLOOM_RENDERS.computeIfAbsent(ticket.renderSetup, BloomRenderList::new).add(ticket);
            }
            SCHEDULED_BLOOM_RENDERS.clear();
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    static void postDraw() {
        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            BLOOM_RENDERS.values().removeIf(BloomRenderList::postDraw);
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    @ApiStatus.Internal
    public static void setupBloomShaderUniforms() {
        final var config = ConfigHolder.INSTANCE.client.bloom;

        // Forcefully insert config values to shader
        modifyBloomPostShaders((index, shader) -> {
            shader.safeGetUniform("DepthNear").set(GameRenderer.PROJECTION_Z_NEAR);
            shader.safeGetUniform("DepthFar").set(Minecraft.getInstance().gameRenderer.getDepthFar());

            // look for blur steps & change their blur strength to match the config
            if (shader.getName().contains("blur")) {
                if (index % 2 == 0) {
                    shader.safeGetUniform("BlurDir").set(0.0f, config.step);
                } else {
                    shader.safeGetUniform("BlurDir").set(config.step, 0.0f);
                }
            }

            shader.safeGetUniform("BloomStrength").set(config.strength);
            shader.safeGetUniform("BaseBrightness").set(config.baseBrightness);
            shader.safeGetUniform("MinBrightness").set(config.minBrightness);
            shader.safeGetUniform("MaxBrightness").set(config.maxBrightness);
        });
    }

    public static void setFilterToggleUniform(final boolean fragmentFilterEnabled) {
        modifyBloomPostShaders((index, shader) -> {
            if (shader.getName().contains("filter_bloom_color")) {
                shader.safeGetUniform("EnableFilter").set(fragmentFilterEnabled ? 1 : 0);
            }
        });
    }

    private static void modifyBloomPostShaders(IntObjectConsumer<EffectInstance> consumer) {
        // Forcefully insert config values to shader
        List<PostPass> passes = ((PostChainAccessor) BloomShaderManager.BLOOM_CHAIN).getPasses();
        for (int i = 0; i < passes.size(); i++) {
            PostPass pass = passes.get(i);
            consumer.accept(i, pass.getEffect());
        }
    }

    /**
     * Invalidate tickets associated with given level.
     *
     * @param level the level that was unloaded
     */
    public static void invalidateLevelData(LevelAccessor level) {
        Objects.requireNonNull(level, "level == null");
        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
                if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == level) {
                    ticket.invalidate();
                }
            }

            for (var e : BLOOM_RENDERS.entrySet()) {
                for (BloomRenderTicket ticket : e.getValue()) {
                    if (ticket.isValid() && ticket.worldContext != null && ticket.worldContext.get() == level) {
                        ticket.invalidate();
                    }
                }
            }
        } finally {
            BLOOM_RENDER_LOCK.readLock().unlock();
        }

        if (GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SAFE_MODE_CONFIG_NAME)) {
            BloomSafeMode.invalidateLevelData();
        }
    }

    public static void invalidateSectionData(SectionPos sectionPos) {
        if (GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SAFE_MODE_CONFIG_NAME)) {
            BloomSafeMode.invalidateSectionData(sectionPos);
        }
    }

    // region vanilla-only code paths for automagic block bloom

    /// @implNote map values are {@link LinkedHashSet}s for iteration order stability
    private static final ThreadLocal<@Nullable Long2ObjectMap<@Nullable Set<QuadCacheEntry>>> quadCache_tl = new ThreadLocal<>();

    private static @Nullable Long2ObjectMap<@Nullable Set<QuadCacheEntry>> getThreadQuadCache() {
        return quadCache_tl.get();
    }

    private static Long2ObjectMap<@Nullable Set<QuadCacheEntry>> getOrCreateThreadQuadCache() {
        var quadCache = getThreadQuadCache();
        if (quadCache == null) {
            quadCache = new Long2ObjectOpenHashMap<>();
            quadCache_tl.set(quadCache);
        }
        return quadCache;
    }

    public static boolean chunkSectionHasBloomQuads(long sectionPos) {
        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            var quadCache = getThreadQuadCache();
            if (quadCache == null) return false;

            return quadCache.containsKey(sectionPos);
        } finally {
            BLOOM_RENDER_LOCK.readLock().unlock();
        }
    }

    private static final ThreadLocal<PoseStack> poseStack_tl = ThreadLocal.withInitial(PoseStack::new);

    public static void drawBlockBloomForChunk(long sectionPos,
                                              Function<RenderType, VertexConsumer> vertexConsumerProvider) {
        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            var quadCache = getThreadQuadCache();
            if (quadCache == null) {
                return;
            }

            Set<QuadCacheEntry> quads = quadCache.remove(sectionPos);
            if (quads == null || quads.isEmpty()) {
                return;
            }
            if (quadCache.isEmpty()) {
                // remove the thread local's value if this thread's map is empty so GC can work on it
                quadCache_tl.remove();
            }

            VertexConsumer bloomVertexConsumer = null;
            VertexConsumer cutoutVertexConsumer = null;

            PoseStack poseStack = poseStack_tl.get();
            for (QuadCacheEntry quad : quads) {
                poseStack.pushPose();
                // push the transformation & normal matrices directly into poseStack.last()
                quad.transformation.get(poseStack.last().pose());
                quad.transformation.normal(poseStack.last().normal());

                if (quad.renderType == GTRenderTypes.bloom()) {
                    if (cutoutVertexConsumer == null)
                        cutoutVertexConsumer = vertexConsumerProvider.apply(RenderType.cutout());

                    // copy quads that are already on the bloom layer to cutout
                    cutoutVertexConsumer.putBulkData(poseStack.last(), quad.quad, quad.brightness,
                            quad.tintG, quad.tintG, quad.tintB, quad.packedLights, quad.packedOverlay, true);
                } else {
                    if (bloomVertexConsumer == null)
                        bloomVertexConsumer = vertexConsumerProvider.apply(GTRenderTypes.bloom());

                    // copy everything else to bloom
                    bloomVertexConsumer.putBulkData(poseStack.last(), quad.quad, quad.brightness,
                            quad.tintG, quad.tintG, quad.tintB, quad.packedLights, quad.packedOverlay, true);
                }

                poseStack.popPose();
            }
        } finally {
            BLOOM_RENDER_LOCK.readLock().unlock();
        }
    }

    /// Helper function for skipping bloom quads drawn with non-bloom render types
    @ApiStatus.Internal
    public static void captureBloomQuad(BakedQuad quad, @Nullable RenderType renderType, BlockPos pos,
                                        Matrix4fc transformation, int[] packedLights, int packedOverlay,
                                        float[] brightness, float tintR, float tintG, float tintB) {
        if (renderType == null || renderType == GTRenderTypes.bloom() ||
                renderType == GTRenderTypes.entityBloomBlockSheet()) {
            return;
        }

        if (TextureMetadataHelper.hasBloom(quad, packedLights)) {
            QuadCacheEntry entry = new QuadCacheEntry(quad, renderType, transformation,
                    packedLights, packedOverlay, brightness, tintR, tintG, tintB);

            BLOOM_RENDER_LOCK.writeLock().lock();
            try {
                Set<QuadCacheEntry> sectionQuads = getOrCreateThreadQuadCache()
                        .computeIfAbsent(SectionPos.asLong(pos), $ -> new LinkedHashSet<>());
                if (!sectionQuads.add(entry)) {
                    GTCEu.LOGGER.warn("Duplicate quad {} on block [{}]???", entry, pos.toShortString());
                }
            } finally {
                BLOOM_RENDER_LOCK.writeLock().unlock();
            }
        }
    }

    // endregion

    private static class BloomRenderList extends ArrayList<BloomRenderTicket> {

        private final @Nullable IRenderSetup renderSetup;

        private BloomRenderList(@Nullable IRenderSetup renderSetup) {
            super(2);
            this.renderSetup = renderSetup;
        }

        private void draw(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context) {
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
        private boolean postDraw() {
            if (this.isEmpty()) return true;

            boolean removedAny = this.removeIf(ticket -> {
                ticket.checkValidity();
                return !ticket.isValid();
            });
            if (!removedAny) return false;

            return this.isEmpty();
        }
    }

    @ApiStatus.Internal
    private record QuadCacheEntry(BakedQuad quad, @Nullable RenderType renderType,
                                  Matrix4fc transformation, int[] packedLights, int packedOverlay,
                                  float[] brightness, float tintR, float tintG, float tintB) {

        @Override
        public String toString() {
            int[][] unpackedLights = Arrays.stream(packedLights)
                    .mapToObj(packed -> new int[] { LightTexture.block(packed), LightTexture.sky(packed) })
                    .toArray(int[][]::new);

            return "{ " +
                    "renderType=" + (renderType != null ? ((RenderStateShardAccessor) renderType).getName() : null) +
                    ", transformation=" + FormattingUtil.matrixToSingleLineString(transformation) +
                    ", lights=" + Arrays.deepToString(unpackedLights) +
                    ", packedOverlay=" + packedOverlay +
                    ", brightness=" + Arrays.toString(brightness) +
                    ", tint=[" + tintR + ", " + tintG + ", " + tintB + ']' +
                    " }";
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            QuadCacheEntry that = (QuadCacheEntry) o;
            return this.renderType == that.renderType &&
                    this.packedOverlay() == that.packedOverlay &&
                    Float.floatToIntBits(this.tintR) == Float.floatToIntBits(that.tintR) &&
                    Float.floatToIntBits(this.tintG) == Float.floatToIntBits(that.tintG) &&
                    Float.floatToIntBits(this.tintB) == Float.floatToIntBits(that.tintB) &&

                    this.transformation.equals(that.transformation) &&
                    Arrays.equals(this.packedLights, that.packedLights) &&
                    Arrays.equals(this.brightness, that.brightness) &&
                    // quad is compared last because it has the slowest equals()
                    this.quad.equals(that.quad);
        }

        @Override
        public int hashCode() {
            int result = this.quad.hashCode();
            result = 31 * result + Objects.hashCode(this.renderType);
            result = 31 * result + this.transformation.hashCode();

            result = 31 * result + Arrays.hashCode(this.packedLights);
            result = 31 * result + this.packedOverlay;

            result = 31 * result + Arrays.hashCode(this.brightness);
            result = 31 * result + Float.hashCode(this.tintR);
            result = 31 * result + Float.hashCode(this.tintG);
            result = 31 * result + Float.hashCode(this.tintB);

            return result;
        }
    }
}
