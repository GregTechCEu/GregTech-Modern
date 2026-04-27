package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.model.BloomMetadataSection;
import com.gregtechceu.gtceu.client.particle.GTParticle;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.client.RenderStateShardAccessor;
import com.gregtechceu.gtceu.core.mixins.client.bloom.PostChainAccessor;
import com.gregtechceu.gtceu.utils.FormattingUtil;

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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class BloomUtil {

    public static RenderLevelStageEvent.@UnknownNullability Stage AFTER_BLOOM_RENDER_STAGE;

    // always outside the world border
    public static final long INVALID_SECTION_POS = SectionPos.asLong(60000000, 60000000, 60000000);

    private static final Map<@Nullable IRenderSetup, BloomRenderList> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    private static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    private static final ReadWriteLock BLOOM_RENDER_LOCK = new ReentrantReadWriteLock();

    /// @implNote values are {@link LinkedHashSet}s for iteration order stability
    private static final Long2ObjectMap<@Nullable Set<QuadCacheEntry>> TEMPORARY_RENDER_QUAD_CACHE = Long2ObjectMaps
            .synchronize(new Long2ObjectOpenHashMap<>());

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
        if (!GTShaders.canUseBloomShader()) return BloomRenderTicket.INVALID;

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
            BLOOM_RENDER_LOCK.readLock().lock();
        }

        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            // Completely dump the temp quad cache when changing dimensions etc.
            TEMPORARY_RENDER_QUAD_CACHE.clear();
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    public static void renderBloom(Camera camera, LevelRenderer levelRenderer,
                                   PoseStack poseStack, Matrix4f projectionMatrix, Frustum frustum,
                                   float partialTicks) {
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();

        GTRenderTypes.bloom().setupRenderState();

        profiler.push("special");

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

        // copy depth buffer from the main render target so bloom won't render through blocks
        // GTShaders.BLOOM_TARGET.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());

        GTShaders.BLOOM_CHAIN.process(partialTicks);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        VertexBuffer.unbind();

        // pop the "special" profiler section before posting forge render stage event
        profiler.pop();

        // noinspection UnstableApiUsage
        ForgeHooksClient.dispatchRenderStage(AFTER_BLOOM_RENDER_STAGE, levelRenderer,
                poseStack, projectionMatrix, levelRenderer.getTicks(), camera, frustum);

        GTRenderTypes.bloom().clearRenderState();
    }

    private static void preDraw() {
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

    private static void postDraw() {
        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            BLOOM_RENDERS.values().removeIf(BloomRenderList::postDraw);
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    private static final String FILTER_TOGGLE_UNIFORM = "EnableFilter";
    private static final String DEPTH_NEAR_UNIFORM = "DepthNear";
    private static final String DEPTH_FAR_UNIFORM = "DepthFar";

    private static final String BLUR_SHADER_NAME = "blur";
    private static final String BLOOM_STRENGTH_UNIFORM = "BloomStrength";
    private static final String BASE_BRIGHTNESS_UNIFORM = "BaseBrightness";
    private static final String MAX_BRIGHTNESS_UNIFORM = "MaxBrightness";
    private static final String MIN_BRIGHTNESS_UNIFORM = "MinBrightness";
    private static final String BLUR_DIR_UNIFORM = "BlurDir";

    @ApiStatus.Internal
    public static void setupBloomShaderUniforms(boolean drawBlockBloom) {
        var config = ConfigHolder.INSTANCE.client.shader;

        // Forcefully insert config values to shader
        List<PostPass> passes = ((PostChainAccessor) GTShaders.BLOOM_CHAIN).getPasses();
        for (int i = 0; i < passes.size(); i++) {
            PostPass pass = passes.get(i);
            EffectInstance shader = pass.getEffect();

            shader.safeGetUniform(FILTER_TOGGLE_UNIFORM).set(drawBlockBloom ? 1 : 0);
            shader.safeGetUniform(DEPTH_NEAR_UNIFORM).set(GameRenderer.PROJECTION_Z_NEAR);
            shader.safeGetUniform(DEPTH_FAR_UNIFORM).set(Minecraft.getInstance().gameRenderer.getDepthFar());

            if (shader.getName().contains(BLUR_SHADER_NAME)) {
                if (i % 2 == 0) {
                    shader.safeGetUniform(BLUR_DIR_UNIFORM).set(0.0f, config.step);
                } else {
                    shader.safeGetUniform(BLUR_DIR_UNIFORM).set(config.step, 0.0f);
                }
            }
            shader.safeGetUniform(BLOOM_STRENGTH_UNIFORM).set(config.strength);
            shader.safeGetUniform(BASE_BRIGHTNESS_UNIFORM).set(config.baseBrightness);
            shader.safeGetUniform(MAX_BRIGHTNESS_UNIFORM).set(config.maxBrightness);
            shader.safeGetUniform(MIN_BRIGHTNESS_UNIFORM).set(config.minBrightness);
        }
    }

    public static boolean chunkSectionHasBloomQuads(long sectionPos) {
        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            return TEMPORARY_RENDER_QUAD_CACHE.containsKey(sectionPos);
        } finally {
            BLOOM_RENDER_LOCK.readLock().unlock();
        }
    }

    private static final ThreadLocal<PoseStack> poseStack_tl = ThreadLocal.withInitial(PoseStack::new);

    public static void drawBlockBloomForChunk(long sectionPos,
                                              Function<RenderType, VertexConsumer> vertexConsumerProvider) {
        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            Set<QuadCacheEntry> quads = TEMPORARY_RENDER_QUAD_CACHE.remove(sectionPos);
            if (quads == null) {
                return;
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

    public static void chunkSectionUnloaded(long sectionPos) {
        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            TEMPORARY_RENDER_QUAD_CACHE.remove(sectionPos);
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    /// Helper function for skipping bloom quads drawn with non-bloom render types
    @ApiStatus.Internal
    public static void captureBloomQuad(BakedQuad quad, @Nullable RenderType renderType, BlockPos pos,
                                        @Nullable Matrix4f transformation, int[] packedLights, int packedOverlay,
                                        float[] brightness, float tintR, float tintG, float tintB) {
        if (renderType == null || renderType == GTRenderTypes.bloom() ||
                renderType == GTRenderTypes.entityBloomBlockSheet()) {
            return;
        }

        if (BloomMetadataSection.hasBloom(quad, packedLights)) {
            if (transformation == null) {
                transformation = new Matrix4f();
                transformation.translate(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
            }
            QuadCacheEntry entry = new QuadCacheEntry(quad, renderType, transformation, packedLights, packedOverlay,
                    brightness, tintR, tintG, tintB);

            BLOOM_RENDER_LOCK.writeLock().lock();
            try {
                Set<QuadCacheEntry> sectionQuads = TEMPORARY_RENDER_QUAD_CACHE.computeIfAbsent(SectionPos.asLong(pos),
                        $ -> new LinkedHashSet<>());
                if (!sectionQuads.add(entry)) {
                    GTCEu.LOGGER.warn("Duplicate quad {} on block [{}]???", entry, pos.toShortString());
                }
            } finally {
                BLOOM_RENDER_LOCK.writeLock().unlock();
            }
        }
    }

    public static final class BloomRenderTicket {

        public static final BloomRenderTicket INVALID = new BloomRenderTicket();

        private final @Nullable IRenderSetup renderSetup;
        private final IBloomEffect render;
        private final @Nullable Predicate<BloomRenderTicket> validityChecker;
        private final @Nullable Supplier<@Nullable Level> worldContext;

        private boolean invalidated;

        private BloomRenderTicket() {
            this(null, (p, b, c) -> {}, null, null);
            this.invalidated = true;
        }

        BloomRenderTicket(@Nullable IRenderSetup renderSetup, IBloomEffect render,
                          @Nullable Predicate<BloomRenderTicket> validityChecker,
                          @Nullable Supplier<@Nullable Level> worldContext) {
            this.renderSetup = renderSetup;
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

    private static class BloomRenderList extends ArrayList<BloomRenderTicket> {

        private final @Nullable IRenderSetup renderSetup;

        private BloomRenderList(@Nullable IRenderSetup renderSetup) {
            super(2);
            this.renderSetup = renderSetup;
        }

        private void draw(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context) {
            boolean initialized = false;

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
                poseStack.translate(-context.camPos().x(), -context.camPos().y(), -context.camPos().z());
                ticket.render.renderBloomEffect(poseStack, buffer, context);
                poseStack.popPose();
            }

            if (initialized && this.renderSetup != null) {
                this.renderSetup.postDraw(buffer);
            }
        }

        /**
         * Do post-draw cleanup such as removing invalidated draw tickets.
         *
         * @return Whether this list is empty and should thus be removed from the tracking map.
         */
        private boolean postDraw() {
            if (this.isEmpty()) return true;

            if (!this.removeIf(ticket -> {
                ticket.checkValidity();
                return !ticket.isValid();
            })) {
                return true;
            }
            return this.isEmpty();
        }
    }

    @ApiStatus.Internal
    private record QuadCacheEntry(BakedQuad quad, @Nullable RenderType renderType,
                                  Matrix4f transformation, int[] packedLights, int packedOverlay,
                                  float[] brightness, float tintR, float tintG, float tintB) {

        @Override
        public String toString() {
            int[][] unpackedLights = Arrays.stream(packedLights)
                    .mapToObj(packed -> new int[]{ LightTexture.block(packed), LightTexture.sky(packed) })
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
