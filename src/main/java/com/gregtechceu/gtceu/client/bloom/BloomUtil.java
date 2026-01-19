package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.client.model.BloomMetadataSection;
import com.gregtechceu.gtceu.client.particle.GTParticle;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.client.PostChainAccessor;
import com.gregtechceu.gtceu.core.mixins.client.VertexBufferAccessor;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class BloomUtil {

    private static final Map<IRenderSetup, List<BloomRenderTicket>> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    private static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    private static final ReadWriteLock BLOOM_RENDER_LOCK = new ReentrantReadWriteLock();

    public static Map<BlockPos, VertexBuffer> BLOOM_BUFFERS = new ConcurrentHashMap<>();
    public static Map<BlockPos, BufferBuilder> BLOOM_BUFFER_BUILDERS = new ConcurrentHashMap<>();
    public static Map<BlockPos, BufferBuilder.SortState> BLOOM_BUFFER_SORT_STATES = new ConcurrentHashMap<>();

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
     * @param setup       Render setup, if exists
     * @param render      Rendering callback
     * @param blockEntity Meta tile entity instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || blockEntity == null}
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull IBloomEffect render,
                                                        @NotNull BlockEntity blockEntity) {
        Objects.requireNonNull(blockEntity, "blockEntity == null");
        return registerBloomRender(setup,
                new IBloomEffect() {

                    @Override
                    public void renderBloomEffect(@NotNull PoseStack poseStack, @NotNull BufferBuilder buffer,
                                                  @NotNull EffectRenderContext context) {
                        render.renderBloomEffect(poseStack, buffer, context);
                    }

                    @Override
                    public boolean shouldRenderBloomEffect(@NotNull EffectRenderContext context) {
                        return blockEntity.getLevel() == context.getRenderViewEntity().level() &&
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
     * @param setup    Render setup, if exists
     * @param render   Rendering callback
     * @param particle Particle instance
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null || metaTileEntity == null}
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull IBloomEffect render,
                                                        @NotNull GTParticle particle) {
        Objects.requireNonNull(particle, "particle == null");
        return registerBloomRender(setup, render, t -> particle.isAlive());
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
     * @param render          Rendering callback
     * @param validityChecker Optional validity checker; returning {@code false} causes the ticket to be invalidated.
     *                        Checked on both pre- / post-render each frame.
     * @return Ticket for the registered bloom render callback
     * @throws NullPointerException if {@code bloomType == null || render == null}
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, BlockEntity)
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, GTParticle)
     * @see #registerBloomRender(IRenderSetup, IBloomEffect, Predicate, Supplier)
     */
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker) {
        return registerBloomRender(setup, render, validityChecker, null);
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
    @NotNull
    public static BloomRenderTicket registerBloomRender(@Nullable IRenderSetup setup,
                                                        @NotNull IBloomEffect render,
                                                        @Nullable Predicate<BloomRenderTicket> validityChecker,
                                                        @Nullable Supplier<Level> worldContext) {
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
    public static void invalidateLevelTickets(@NotNull LevelAccessor level) {
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
    }

    public static void init() {}

    public static void renderBloom(Camera camera, @NotNull Entity entity, LevelRenderer levelRenderer,
                                   PoseStack poseStack, Matrix4f projectionMatrix, Frustum frustum,
                                   float partialTicks) {
        if (!GTShaders.canUseBloomShader()) {
            return;
        }
        Vec3 camPos = camera.getPosition();
        Minecraft.getInstance().getProfiler().popPush("gtceu:bloom");

        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            GTRenderTypes.bloom().setupRenderState();
            // copy depth buffer from the main render target so bloom won't render through blocks
            GTShaders.BLOOM_TARGET.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());

            preDraw();
            if (!BLOOM_RENDERS.isEmpty()) {
                EffectRenderContext context = EffectRenderContext.getInstance()
                        .update(entity, camPos, frustum, partialTicks);

                for (List<BloomRenderTicket> list : BLOOM_RENDERS.values()) {
                    BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                    draw(poseStack, buffer, context, list);
                }
                postDraw();
            }

            if (ConfigHolder.INSTANCE.client.shader.emissiveTexturesHaveBloom) {
                setupBloomUniforms(true);
                drawBlockBloom(poseStack, projectionMatrix, camPos);
            } else {
                setupBloomUniforms(false);
            }

            render(partialTicks, poseStack, projectionMatrix, levelRenderer, camera, frustum);
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    private static void preDraw() {
        for (BloomRenderTicket ticket : SCHEDULED_BLOOM_RENDERS) {
            if (!ticket.isValid()) continue;
            BLOOM_RENDERS.computeIfAbsent(ticket.renderSetup, k -> new ArrayList<>()).add(ticket);
        }
        SCHEDULED_BLOOM_RENDERS.clear();
    }

    private static void draw(@NotNull PoseStack poseStack, @NotNull BufferBuilder buffer,
                             @NotNull EffectRenderContext context, @NotNull List<BloomRenderTicket> tickets) {
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

            poseStack.pushPose();
            poseStack.translate(-context.camPos().x(), -context.camPos().y(), -context.camPos().z());
            ticket.render.renderBloomEffect(poseStack, buffer, context);
            poseStack.popPose();
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

    public static void finishBloomBuffer(BlockPos pos, BufferBuilder builder) {
        BufferBuilder.RenderedBuffer buffer = builder.endOrDiscardIfEmpty();
        if (buffer != null) {
            BLOOM_BUFFER_BUILDERS.remove(pos, builder);
            BLOOM_BUFFER_SORT_STATES.put(pos, builder.getSortState());

            if (RenderSystem.isOnRenderThread()) {
                VertexBuffer vertexBuffer = BLOOM_BUFFERS.computeIfAbsent(pos,
                        $ -> new VertexBuffer(VertexBuffer.Usage.STATIC));
                BloomUtil.uploadBloomBuffer(buffer, vertexBuffer);
            } else {
                RenderSystem.recordRenderCall(() -> {
                    VertexBuffer vertexBuffer = BLOOM_BUFFERS.computeIfAbsent(pos,
                            $ -> new VertexBuffer(VertexBuffer.Usage.STATIC));
                    BloomUtil.uploadBloomBuffer(buffer, vertexBuffer);
                });
            }
        }
    }

    public static void uploadBloomBuffer(BufferBuilder.RenderedBuffer builder, VertexBuffer buffer) {
        if (!buffer.isInvalid()) {
            buffer.bind();
            buffer.upload(builder);
            VertexBuffer.unbind();
        }
    }

    public static void removeBloomChunk(BlockPos origin) {
        BLOOM_BUFFER_BUILDERS.remove(origin);
        BLOOM_BUFFER_SORT_STATES.remove(origin);
        VertexBuffer buffer = BLOOM_BUFFERS.remove(origin);
        if (buffer != null) {
            if (!RenderSystem.isOnRenderThread()) {
                RenderSystem.recordRenderCall(buffer::close);
            } else {
                buffer.close();
            }
        }
    }

    public static BufferBuilder getOrStartBloomBuffer(BlockPos pos) {
        BufferBuilder builder = BLOOM_BUFFER_BUILDERS.computeIfAbsent(pos,
                $ -> new BufferBuilder(GTRenderTypes.bloom().bufferSize()));
        if (!builder.building()) {
            builder.begin(GTRenderTypes.bloom().mode(), GTRenderTypes.bloom().format());
        }
        return builder;
    }

    public static void bakeBloomChunkBuffers(BlockPos pos, Vec3 camPos) {
        if (!GTShaders.canUseBloomShader()) {
            return;
        }

        BufferBuilder builder = BLOOM_BUFFER_BUILDERS.get(pos);
        if (builder == null || !builder.building()) {
            return;
        }
        builder.setQuadSorting(VertexSorting.byDistance((float) camPos.x() - pos.getX(),
                (float) camPos.y() - pos.getY(), (float) camPos.z() - pos.getZ()));

        finishBloomBuffer(pos, builder);
    }

    public static ThreadLocal<BlockPos> CURRENT_RENDERING_CHUNK_POS = new ThreadLocal<>();

    private static final String FILTER_TOGGLE_UNIFORM = "EnableFilter";
    private static final String DEPTH_NEAR_UNIFORM = "DepthNear";
    private static final String DEPTH_FAR_UNIFORM = "DepthFar";

    private static final String BLUR_SHADER_NAME = "blur";
    private static final String BLOOM_STRENGTH_UNIFORM = "BloomStrength";
    private static final String BASE_BRIGHTNESS_UNIFORM = "BaseBrightness";
    private static final String MAX_BRIGHTNESS_UNIFORM = "MaxBrightness";
    private static final String MIN_BRIGHTNESS_UNIFORM = "MinBrightness";
    private static final String BLUR_DIR_UNIFORM = "BlurDir";

    private static void setupBloomUniforms(boolean drawBlockBloom) {
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

    private static void drawBlockBloom(PoseStack poseStack, Matrix4f projectionMatrix, Vec3 camPos) {
        for (var entry : BLOOM_BUFFERS.entrySet()) {
            BlockPos pos = entry.getKey();
            VertexBuffer buffer = entry.getValue();

            // return early if buffer is invalid or has no vertex data bound
            // VertexBuffer#mode's nullness is the easiest way to check this.
            if (buffer.isInvalid() || ((VertexBufferAccessor) buffer).getMode() == null) {
                continue;
            }
            buffer.bind();

            poseStack.pushPose();
            poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
            poseStack.translate(-camPos.x(), -camPos.y(), -camPos.z());

            //noinspection DataFlowIssue
            buffer.drawWithShader(poseStack.last().pose(), projectionMatrix,
                    GameRenderer.getRendertypeCutoutShader());
            poseStack.popPose();
        }
    }

    private static void render(float partialTicks, PoseStack poseStack, Matrix4f projectionMatrix,
                               LevelRenderer levelRenderer, Camera camera, Frustum frustum) {
        GTShaders.BLOOM_CHAIN.process(partialTicks);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        VertexBuffer.unbind();

        Minecraft.getInstance().getProfiler().pop();

        // noinspection UnstableApiUsage
        ForgeHooksClient.dispatchRenderStage(GTRenderTypes.bloom(), levelRenderer,
                poseStack, projectionMatrix, levelRenderer.getTicks(), camera, frustum);

        GTRenderTypes.bloom().clearRenderState();
    }

    public static void copyToBloomBuffer(VertexConsumer consumer, PoseStack.Pose pose, BakedQuad quad,
                                         float[] colorMuls, float red, float green, float blue,
                                         int[] combinedLights, int combinedOverlay, boolean mulColor,
                                         Operation<Void> original) {
        original.call(consumer, pose, quad,
                colorMuls, red, green, blue,
                combinedLights, combinedOverlay, mulColor);
        if (!GTShaders.canUseBloomShader()) {
            return;
        }

        BlockPos chunkOrigin = BloomUtil.CURRENT_RENDERING_CHUNK_POS.get();
        if (chunkOrigin != null && BloomMetadataSection.hasBloom(quad, combinedLights)) {
            original.call(BloomUtil.getOrStartBloomBuffer(chunkOrigin), pose, quad,
                    colorMuls, red, green, blue,
                    combinedLights, combinedOverlay, mulColor);
        }
    }

    public static final class BloomRenderTicket {

        public static final BloomRenderTicket INVALID = new BloomRenderTicket();

        @Nullable
        private final IRenderSetup renderSetup;
        private final IBloomEffect render;
        @Nullable
        private final Predicate<BloomRenderTicket> validityChecker;
        @Nullable
        private final Supplier<Level> worldContext;

        private boolean invalidated;

        BloomRenderTicket() {
            this(null, (p, b, c) -> {}, null, null);
            this.invalidated = true;
        }

        BloomRenderTicket(@Nullable IRenderSetup renderSetup, @NotNull IBloomEffect render,
                          @Nullable Predicate<BloomRenderTicket> validityChecker,
                          @Nullable Supplier<Level> worldContext) {
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

    @ApiStatus.Internal
    @FunctionalInterface
    public interface RegionVisibilityTest {

        boolean isAxisAlignedWith(int x, int y, int z);
    }
}
