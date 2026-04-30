package com.gregtechceu.gtceu.client.bloom;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.gregtechceu.gtceu.client.shader.GTShaders;
import com.gregtechceu.gtceu.client.util.TextureMetadataHelper;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.mojang.blaze3d.pipeline.RenderCall;
import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.SectionPos;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static com.gregtechceu.gtceu.client.bloom.BloomUtil.*;

/**
 * A 'safe mode' for bloom rendering that's less intrusive but slower than the normal implementation.
 */
public class BloomSafeMode {

    // it's most likely better to use ConcurrentHashMaps rather than synchronized Long2ObjectMaps for this
    // even with the boxing overhead
    public static Map<SectionPos, VertexBuffer> BLOOM_BUFFERS = new ConcurrentHashMap<>();
    public static Map<SectionPos, BufferBuilder> BLOOM_BUFFER_BUILDERS = new ConcurrentHashMap<>();
    public static Map<SectionPos, BufferBuilder.SortState> BLOOM_BUFFER_SORT_STATES = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    public static ThreadLocal<@Nullable SectionPos> CURRENT_RENDERING_SECTION = new ThreadLocal<>();

    public static void drawBlockBloom(Camera camera, PoseStack poseStack, Frustum frustum, Matrix4f projectionMatrix,
                                      LevelRenderer levelRenderer, ProfilerFiller profilerFiller) {
        Vec3 camPos = camera.getPosition();
        profilerFiller.push("safe_mode");

        ShaderInstance shader = setupBlockShaderUniforms(poseStack, projectionMatrix);
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
            chunkOffset.set(0, 0, 0);
        }
        shader.clear();
        VertexBuffer.unbind();

        // pop the "safe_mode" profiler section before posting forge render stage event
        profilerFiller.pop();

        // noinspection UnstableApiUsage
        ForgeHooksClient.dispatchRenderStage(AFTER_BLOOM_RENDER_STAGE, levelRenderer, poseStack, projectionMatrix,
                levelRenderer.getTicks(), camera, frustum);
    }

    public static void copyToBloomBuffer(VertexConsumer originalVertexConsumer, BakedQuad quad, int[] combinedLights,
                                         Consumer<VertexConsumer> draw) {
        draw.accept(originalVertexConsumer);

        if (!ConfigHolder.INSTANCE.client.bloom.safeMode || !GTShaders.canUseBloomShader()) {
            return;
        }

        SectionPos sectionOrigin = CURRENT_RENDERING_SECTION.get();
        if (sectionOrigin != null && TextureMetadataHelper.hasBloom(quad, combinedLights)) {
            draw.accept(getOrStartBloomBuffer(sectionOrigin));
        }
    }

    public static void finishBloomBuffer(SectionPos sectionPos, BufferBuilder builder) {
        BufferBuilder.RenderedBuffer buffer = builder.endOrDiscardIfEmpty();
        if (buffer == null) {
            return;
        }

        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            BLOOM_BUFFER_BUILDERS.remove(sectionPos, builder);
            BLOOM_BUFFER_SORT_STATES.put(sectionPos, builder.getSortState());

            RenderCall upload = () -> {
                VertexBuffer vertexBuffer = BLOOM_BUFFERS.computeIfAbsent(sectionPos,
                        $ -> new VertexBuffer(VertexBuffer.Usage.STATIC));
                uploadBloomBuffer(buffer, vertexBuffer);
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

    public static void uploadBloomBuffer(BufferBuilder.RenderedBuffer builder, VertexBuffer buffer) {
        if (!buffer.isInvalid()) {
            buffer.bind();
            buffer.upload(builder);
            VertexBuffer.unbind();
        }
    }

    public static BufferBuilder getOrStartBloomBuffer(SectionPos sectionPos) {
        BufferBuilder builder = BLOOM_BUFFER_BUILDERS.computeIfAbsent(sectionPos,
                $ -> new BufferBuilder(GTRenderTypes.bloom().bufferSize()));
        if (!builder.building()) {
            builder.begin(GTRenderTypes.bloom().mode(), GTRenderTypes.bloom().format());
        }
        return builder;
    }

    public static void bakeBloomChunkBuffers(SectionPos sectionPos, Vec3 camPos) {
        if (!ConfigHolder.INSTANCE.client.bloom.safeMode || !GTShaders.canUseBloomShader()) {
            return;
        }

        BufferBuilder builder = BLOOM_BUFFER_BUILDERS.get(sectionPos);
        if (builder == null || !builder.building()) {
            return;
        }
        builder.setQuadSorting(VertexSorting.byDistance(
                (float) camPos.x() - sectionPos.minBlockX(),
                (float) camPos.y() - sectionPos.minBlockY(),
                (float) camPos.z() - sectionPos.minBlockZ()
        ));

        finishBloomBuffer(sectionPos, builder);
    }

    /// @return the shader to use for drawing block bloom.
    private static ShaderInstance setupBlockShaderUniforms(PoseStack poseStack, Matrix4f projectionMatrix) {
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

    static void invalidateLevelData() {
        BLOOM_RENDER_LOCK.writeLock().lock();
        try {
            BLOOM_BUFFERS.clear();
            BLOOM_BUFFER_BUILDERS.clear();
            BLOOM_BUFFER_SORT_STATES.clear();
        } finally {
            BLOOM_RENDER_LOCK.writeLock().unlock();
        }
    }

    public static void invalidateSectionData(SectionPos sectionPos) {
        BLOOM_RENDER_LOCK.writeLock().lock();

        try {
            BLOOM_BUFFER_BUILDERS.remove(sectionPos);
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
