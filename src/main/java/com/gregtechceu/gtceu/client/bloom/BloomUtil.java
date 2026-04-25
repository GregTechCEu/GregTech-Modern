package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.client.model.BloomMetadataSection;
import com.gregtechceu.gtceu.client.particle.GTParticle;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.mixins.client.bloom.PostChainAccessor;
import com.gregtechceu.gtceu.core.mixins.client.bloom.VertexBufferAccessor;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderLevelStageEvent;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Matrix4f;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class BloomUtil {

    private static final Map<@Nullable IRenderSetup, List<BloomRenderTicket>> BLOOM_RENDERS = new Object2ObjectOpenHashMap<>();
    private static final List<BloomRenderTicket> SCHEDULED_BLOOM_RENDERS = new ArrayList<>();

    private static final ReadWriteLock BLOOM_RENDER_LOCK = new ReentrantReadWriteLock();

    public static Map<SectionPos, VertexBuffer> BLOOM_BUFFERS = new ConcurrentHashMap<>();
    public static Map<SectionPos, BufferBuilder> BLOOM_BUFFER_BUILDERS = new ConcurrentHashMap<>();
    public static Map<SectionPos, BufferBuilder.SortState> BLOOM_BUFFER_SORT_STATES = new ConcurrentHashMap<>();

    public static RenderLevelStageEvent.@UnknownNullability Stage AFTER_BLOOM_RENDER_STAGE;

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
    public static void invalidateLevelTickets(LevelAccessor level) {
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

    public static void renderBloom(Camera camera, LevelRenderer levelRenderer,
                                   PoseStack poseStack, Matrix4f projectionMatrix, Frustum frustum,
                                   float partialTicks) {
        if (!GTShaders.canUseBloomShader()) {
            return;
        }
        ProfilerFiller profiler = Minecraft.getInstance().getProfiler();
        Vec3 camPos = camera.getPosition();

        GTRenderTypes.bloom().setupRenderState();

        profiler.popPush("gtceu:bloom");

        preDraw();
        if (!BLOOM_RENDERS.isEmpty()) {
            EffectRenderContext context = EffectRenderContext.getInstance()
                    .update(camera, frustum, partialTicks);

            BLOOM_RENDER_LOCK.readLock().lock();
            try {
                BLOOM_RENDERS.forEach((renderSetup, list) -> {
                    BufferBuilder buffer = Tesselator.getInstance().getBuilder();
                    draw(poseStack, buffer, context, renderSetup, list);
                });
            } finally {
                BLOOM_RENDER_LOCK.readLock().unlock();
            }

            postDraw();
        }

        if (ConfigHolder.INSTANCE.client.shader.emissiveTexturesHaveBloom) {
            setupBloomUniforms(true);
            drawBlockBloom(poseStack, projectionMatrix, camPos);
        } else {
            setupBloomUniforms(false);
        }
        // copy depth buffer from the main render target so bloom won't render through blocks
        // GTShaders.BLOOM_TARGET.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());

        GTShaders.BLOOM_CHAIN.process(partialTicks);
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
        VertexBuffer.unbind();

        // the profiler section is popped by popPush() in the calling method so we won't pop it here.

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
                BLOOM_RENDERS.computeIfAbsent(ticket.renderSetup, k -> new ArrayList<>()).add(ticket);
            }
            SCHEDULED_BLOOM_RENDERS.clear();
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    private static void draw(PoseStack poseStack, BufferBuilder buffer, EffectRenderContext context,
                             @Nullable IRenderSetup renderSetup, List<BloomRenderTicket> tickets) {
        boolean initialized = false;

        for (BloomRenderTicket ticket : tickets) {
            ticket.checkValidity();
            if (!ticket.isValid() || !ticket.render.shouldRenderBloomEffect(context)) continue;

            if (!initialized) {
                initialized = true;
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
        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
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
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    public static void finishBloomBuffer(SectionPos pos, BufferBuilder builder) {
        BufferBuilder.RenderedBuffer buffer = builder.endOrDiscardIfEmpty();
        if (buffer == null) {
            return;
        }

        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
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
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    public static void uploadBloomBuffer(BufferBuilder.RenderedBuffer builder, VertexBuffer buffer) {
        if (!buffer.isInvalid()) {
            buffer.bind();
            buffer.upload(builder);
            VertexBuffer.unbind();
        }
    }

    public static void removeBloomChunk(SectionPos origin) {
        BLOOM_RENDER_LOCK.writeLock().lock();

        try {
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
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    public static BufferBuilder getOrStartBloomBuffer(SectionPos pos) {
        BufferBuilder builder = BLOOM_BUFFER_BUILDERS.computeIfAbsent(pos,
                $ -> new BufferBuilder(GTRenderTypes.bloom().bufferSize()));
        if (!builder.building()) {
            builder.begin(GTRenderTypes.bloom().mode(), GTRenderTypes.bloom().format());
        }
        return builder;
    }

    public static void bakeBloomChunkBuffers(SectionPos pos, Vec3 camPos) {
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

    @ApiStatus.Internal
    public static ThreadLocal<@Nullable SectionPos> CURRENT_RENDERING_SECTION = new ThreadLocal<>();

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
        ShaderInstance shader = setupShaderUniforms(poseStack, projectionMatrix);
        Uniform chunkOffsetUniform = shader.CHUNK_OFFSET;

        BLOOM_RENDER_LOCK.readLock().lock();
        try {
            for (var entry : BLOOM_BUFFERS.entrySet()) {
                SectionPos pos = entry.getKey();
                VertexBuffer buffer = entry.getValue();

                // return early if buffer is invalid or has no vertex data bound
                // VertexBuffer#mode's nullness is the easiest way to check this.
                if (buffer.isInvalid() || ((VertexBufferAccessor) buffer).getMode() == null) {
                    continue;
                }

                if (chunkOffsetUniform != null) {
                    chunkOffsetUniform.set(SectionPos.sectionToBlockCoord(pos.getX()) - (float) camPos.x(),
                            SectionPos.sectionToBlockCoord(pos.getY()) - (float) camPos.y(),
                            SectionPos.sectionToBlockCoord(pos.getZ()) - (float) camPos.z());
                    chunkOffsetUniform.upload();
                }

                buffer.bind();
                buffer.draw();
            }
        } finally {
            BLOOM_RENDER_LOCK.readLock().unlock();
        }

        if (chunkOffsetUniform != null) {
            chunkOffsetUniform.set(0, 0, 0);
        }
        shader.clear();
        VertexBuffer.unbind();
    }

    private static ShaderInstance setupShaderUniforms(PoseStack poseStack, Matrix4f projectionMatrix) {
        ShaderInstance shader = RenderSystem.getShader();
        assert shader != null;

        for(int i = 0; i < 12; ++i) {
            int textureId = RenderSystem.getShaderTexture(i);
            shader.setSampler("Sampler" + i, textureId);
        }
        if (shader.MODEL_VIEW_MATRIX != null) shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
        if (shader.PROJECTION_MATRIX != null) shader.PROJECTION_MATRIX.set(projectionMatrix);
        if (shader.COLOR_MODULATOR != null) shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        if (shader.GLINT_ALPHA != null) shader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        if (shader.FOG_START != null) shader.FOG_START.set(RenderSystem.getShaderFogStart());
        if (shader.FOG_END != null) shader.FOG_END.set(RenderSystem.getShaderFogEnd());
        if (shader.FOG_COLOR != null) shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        if (shader.FOG_SHAPE != null) shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        if (shader.TEXTURE_MATRIX != null) shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        if (shader.GAME_TIME != null) shader.GAME_TIME.set(RenderSystem.getShaderGameTime());

        RenderSystem.setupShaderLights(shader);
        shader.apply();

        return shader;
    }

    public static void copyToBloomBuffer(VertexConsumer originalVertexConsumer, BakedQuad quad, int[] combinedLights,
                                         Consumer<VertexConsumer> draw) {
        draw.accept(originalVertexConsumer);

        if (!GTShaders.canUseBloomShader()) {
            return;
        }

        SectionPos sectionOrigin = BloomUtil.CURRENT_RENDERING_SECTION.get();
        if (sectionOrigin != null && BloomMetadataSection.hasBloom(quad, combinedLights)) {
            draw.accept(BloomUtil.getOrStartBloomBuffer(sectionOrigin));
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
}
