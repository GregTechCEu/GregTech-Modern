package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.core.config.GTEarlyConfig;
import com.gregtechceu.gtceu.core.mixins.GTMixinPlugin;
import com.gregtechceu.gtceu.core.mixins.client.bloom.BufferBuilderAccessor;
import com.gregtechceu.gtceu.core.mixins.client.bloom.LevelRendererAccessor;
import com.gregtechceu.gtceu.utils.ScopedValue;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;

import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.client.bloom.BloomShaderManager.BLOOM_TARGET;

/**
 * The actual rendering logic for bloom
 */
@ApiStatus.Internal
@UtilityClass
public class BloomRenderer {

    static final ReadWriteLock BLOOM_RENDER_LOCK = new ReentrantReadWriteLock();

    @Accessors(fluent = true)
    @Getter
    @ApiStatus.Internal
    private static final ThreadLocal<ScopedValue.Object<Supplier<VertexConsumer>>> bloomChunkContext = ThreadLocal
            .withInitial(ScopedValue.Object::new);

    static void renderBloom(Camera camera, PoseStack poseStack, Frustum frustum,
                            Matrix4f frustumMatrix, Matrix4f projectionMatrix, float partialTicks,
                            LevelRenderer levelRenderer, ProfilerFiller profilerFiller) {
        if (!BloomShaderManager.isBloomActive()) return;

        Vec3 camPos = camera.getPosition();

        profilerFiller.popPush("gtceu:bloom");
        setupBloomShaderUniforms();
        GTRenderTypes.bloom().setupRenderState();

        renderSpecialBloom(camera, poseStack, frustum, partialTicks, profilerFiller);

        // safe mode disabled -> use deeper, faster hackery
        if (!BloomRenderer.SafeMode.enabled()) {
            ((LevelRendererAccessor) levelRenderer).invokeRenderSectionLayer(GTRenderTypes.bloom(),
                    camPos.x, camPos.y, camPos.z, frustumMatrix, projectionMatrix);

            // have to re-setup here. so sad. very aw.
            GTRenderTypes.bloom().setupRenderState();
        }
        // safe mode enabled -> don't draw block bloom the 'normal' way; use BloomSafeMode.drawBlockBloom instead
        else {
            SafeMode.drawBlockBloom(camera, poseStack, frustum, frustumMatrix, projectionMatrix, levelRenderer,
                    profilerFiller);
        }

        processPostEffect(partialTicks, profilerFiller);

        // clear state. again.
        GTRenderTypes.bloom().clearRenderState();

        // profiler section is popped by popPush() in the calling function; don't pop it here
    }

    private static void renderSpecialBloom(Camera camera, PoseStack poseStack, Frustum frustum, float partialTicks,
                                   ProfilerFiller profilerFiller) {
        profilerFiller.push("special");

        // render state is set up & cleared in calling function

        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            BloomHandler.initializeScheduledRenders();
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
        if (!BloomHandler.BLOOM_RENDERS.isEmpty()) {
            EffectRenderContext context = EffectRenderContext.getInstance().update(camera, frustum, partialTicks);

            BLOOM_RENDER_LOCK.readLock().lock();
            try {
                BloomHandler.BLOOM_RENDERS.forEach((renderSetup, list) -> {
                    list.draw(poseStack, context);
                });
            } finally {
                BLOOM_RENDER_LOCK.readLock().unlock();
            }

            BLOOM_RENDER_LOCK.writeLock().lock();
            try {
                BloomHandler.removeInvalidatedRenders();
            } finally {
                BLOOM_RENDER_LOCK.writeLock().unlock();
            }
        }

        profilerFiller.pop();
    }

    private static void processPostEffect(float partialTicks, ProfilerFiller profilerFiller) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget mainTarget = minecraft.getMainRenderTarget();

        profilerFiller.push("processPostEffect");

        BloomShaderManager.BLOOM_CHAIN.process(partialTicks);

        mainTarget.bindWrite(false);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        BLOOM_TARGET.blitToScreen(mainTarget.width, mainTarget.height, false);
        BLOOM_TARGET.unbindRead();

        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();

        profilerFiller.pop();
    }

    private static void setupBloomShaderUniforms() {
        final var config = ConfigHolder.INSTANCE.client.bloom;

        // Forcefully insert config values to shader
        BloomShaderManager.BLOOM_CHAIN.setUniform("DepthNear", GameRenderer.PROJECTION_Z_NEAR);
        BloomShaderManager.BLOOM_CHAIN.setUniform("DepthFar", Minecraft.getInstance().gameRenderer.getDepthFar());

        BloomShaderManager.BLOOM_CHAIN.setUniform("RadiusMultiplier", config.step);

        BloomShaderManager.BLOOM_CHAIN.setUniform("BloomStrength", config.strength);
        BloomShaderManager.BLOOM_CHAIN.setUniform("BaseBrightness", config.baseBrightness);
        BloomShaderManager.BLOOM_CHAIN.setUniform("MinBrightness", config.minBrightness);
        BloomShaderManager.BLOOM_CHAIN.setUniform("MaxBrightness", config.maxBrightness);
    }

    /// Helper function for copying bloom-enabled quads drawn with non-bloom render types
    public static void copyBloomQuad(BakedQuad quad, int[] packedLights, @Nullable RenderType renderType,
                                     Consumer<VertexConsumer> drawConsumer) {
        if (renderType == GTRenderTypes.bloom() || renderType == GTRenderTypes.entityBloomBlockSheet()) {
            return;
        }

        if (TextureMetadataHelper.hasBloom(quad, packedLights)) {
            Supplier<VertexConsumer> currentVertexConsumer = bloomChunkContext().get().getValue();
            if (currentVertexConsumer == null) return;

            drawConsumer.accept(currentVertexConsumer.get());
        }
    }

    /**
     * A 'safe mode' for bloom rendering that's less intrusive but slower than the normal implementation.
     */
    @ApiStatus.Internal
    @UtilityClass
    public static class SafeMode {

        // it's most likely better to use ConcurrentHashMaps rather than synchronized Long2ObjectMaps for this
        // even with the boxing overhead
        public static Map<SectionPos, VertexBuffer> BLOOM_BUFFERS = new ConcurrentHashMap<>();
        public static Map<SectionPos, BufferBuilder> BLOOM_BUFFER_BUILDERS = new ConcurrentHashMap<>();
        public static Map<SectionPos, ByteBufferBuilder> BLOOM_BYTE_BUFFERS = new ConcurrentHashMap<>();
        public static Map<SectionPos, MeshData.SortState> BLOOM_BUFFER_SORT_STATES = new ConcurrentHashMap<>();

        public static boolean enabled() {
            return GTMixinPlugin.isOptionEnabled(GTEarlyConfig.SAFE_MODE);
        }

        @SubscribeEvent
        public static void registerSafeModeChunkGeometryRenderer(AddSectionGeometryEvent event) {
            if (!BloomRenderer.SafeMode.enabled()) return;
            if (!BloomShaderManager.isBloomActive()) return;

            final BlockPos sectionMinBlock = event.getSectionOrigin().immutable();
            final SectionPos sectionOrigin = SectionPos.of(sectionMinBlock);

            event.addRenderer(context -> {
                if (!BloomRenderer.SafeMode.BLOOM_BUFFER_BUILDERS.containsKey(sectionOrigin)) {
                    return;
                }

                Vector3f camPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition().toVector3f();
                camPos = camPos.sub(sectionMinBlock.getX(), sectionMinBlock.getY(), sectionMinBlock.getZ());
                VertexSorting vertexSorting = VertexSorting.byDistance(camPos);

                BloomRenderer.SafeMode.bakeBloomChunkBuffers(sectionOrigin, vertexSorting);
            });
        }

        private static void drawBlockBloom(Camera camera, PoseStack poseStack, Frustum frustum,
                                           Matrix4f frustumMatrix, Matrix4f projectionMatrix,
                                           LevelRenderer levelRenderer, ProfilerFiller profilerFiller) {
            // re-setup in case someone touched-a my spaghetti
            GTRenderTypes.bloom().setupRenderState();

            Vec3 camPos = camera.getPosition();
            profilerFiller.push("safe_mode");

            ShaderInstance shader = setupBlockShaderUniforms(frustumMatrix, projectionMatrix);
            Uniform chunkOffset = shader.CHUNK_OFFSET;

            BLOOM_RENDER_LOCK.readLock().lock();
            try {
                for (var entry : BLOOM_BUFFERS.entrySet()) {
                    SectionPos sectionPos = entry.getKey();
                    VertexBuffer buffer = entry.getValue();

                    // noinspection ConstantValue it just isn't annotated :))
                    if (buffer.isInvalid() || buffer.getFormat() == null) {
                        // return early if buffer is invalid or has no vertex data bound
                        continue;
                    }

                    if (chunkOffset != null) {
                        chunkOffset.set(sectionPos.minBlockX() - (float) camPos.x(),
                                sectionPos.minBlockY() - (float) camPos.y(),
                                sectionPos.minBlockZ() - (float) camPos.z());
                        chunkOffset.upload();
                    }

                    buffer.bind();
                    buffer.draw();
                }
            } finally {
                BLOOM_RENDER_LOCK.readLock().unlock();
            }

            if (chunkOffset != null) {
                chunkOffset.set(0.0f, 0.0f, 0.0f);
            }
            shader.clear();
            VertexBuffer.unbind();

            // pop the "safe_mode" profiler section before posting forge render stage event
            profilerFiller.pop();

            ClientHooks.dispatchRenderStage(BloomHandler.RenderStage.AFTER_BLOOM, levelRenderer,
                    poseStack, frustumMatrix, projectionMatrix, levelRenderer.getTicks(), camera, frustum);
        }

        private static void finishBloomBuffer(SectionPos sectionPos, MeshData mesh, BufferBuilder builder,
                                             VertexSorting vertexSorting) {
            BLOOM_RENDER_LOCK.writeLock().lock();
            try {
                BLOOM_BUFFER_BUILDERS.remove(sectionPos, builder);

                ByteBufferBuilder byteBuffer = BLOOM_BYTE_BUFFERS.remove(sectionPos);
                if (byteBuffer != null) {
                    MeshData.SortState sortState = mesh.sortQuads(byteBuffer, vertexSorting);
                    assert sortState != null;
                    BLOOM_BUFFER_SORT_STATES.put(sectionPos, sortState);
                }

                RenderCall upload = () -> {
                    VertexBuffer vertexBuffer = BLOOM_BUFFERS.computeIfAbsent(sectionPos,
                            $ -> new VertexBuffer(VertexBuffer.Usage.STATIC));
                    uploadBloomBuffer(mesh, vertexBuffer);
                };
                if (RenderSystem.isOnRenderThread()) {
                    upload.execute();
                } else {
                    RenderSystem.recordRenderCall(upload);
                }
            } finally {
                BLOOM_RENDER_LOCK.writeLock().unlock();
            }
        }

        private static void uploadBloomBuffer(MeshData mesh, VertexBuffer buffer) {
            if (!buffer.isInvalid()) {
                buffer.bind();
                buffer.upload(mesh);
                VertexBuffer.unbind();
            }
        }

        public static BufferBuilder getOrStartBloomBuffer(SectionPos sectionPos) {
            return BLOOM_BUFFER_BUILDERS.computeIfAbsent(sectionPos, $ -> {
                ByteBufferBuilder byteBuffer = BLOOM_BYTE_BUFFERS.computeIfAbsent(sectionPos,
                        unused -> new ByteBufferBuilder(GTRenderTypes.bloom().bufferSize()));

                return new BufferBuilder(byteBuffer, GTRenderTypes.bloom().mode(), GTRenderTypes.bloom().format());
            });
        }

        public static void bakeBloomChunkBuffers(SectionPos sectionPos, VertexSorting vertexSorting) {
            if (!BloomShaderManager.isBloomActive()) return;

            BufferBuilder builder = BLOOM_BUFFER_BUILDERS.get(sectionPos);
            if (builder == null || !((BufferBuilderAccessor) builder).isBuilding()) return;

            MeshData mesh = builder.build();
            if (mesh == null) return;

            finishBloomBuffer(sectionPos, mesh, builder, vertexSorting);
        }

        /// @return the shader to use for drawing block bloom.
        private static ShaderInstance setupBlockShaderUniforms(Matrix4f frustumMatrix, Matrix4f projectionMatrix) {
            ShaderInstance shader = RenderSystem.getShader();
            assert shader != null;

            shader.setDefaultUniforms(VertexFormat.Mode.QUADS, frustumMatrix, projectionMatrix,
                    Minecraft.getInstance().getWindow());
            shader.apply();

            return shader;
        }

        static void invalidateLevelData() {
            BLOOM_RENDER_LOCK.writeLock().lock();
            try {
                BLOOM_BUFFERS.clear();
                BLOOM_BUFFER_BUILDERS.clear();
                BLOOM_BYTE_BUFFERS.clear();
                BLOOM_BUFFER_SORT_STATES.clear();
            } finally {
                BLOOM_RENDER_LOCK.writeLock().unlock();
            }
        }

        static void invalidateSectionData(SectionPos sectionPos) {
            BLOOM_RENDER_LOCK.writeLock().lock();

            try {
                BLOOM_BUFFER_BUILDERS.remove(sectionPos);
                BLOOM_BYTE_BUFFERS.remove(sectionPos);
                BLOOM_BUFFER_SORT_STATES.remove(sectionPos);
                VertexBuffer buffer = BLOOM_BUFFERS.remove(sectionPos);

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
    }
}
