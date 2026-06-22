package com.gregtechceu.gtceu.client.renderer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.client.mui.schema.MutableSchema;

import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;

import brachy.modularui.ModularUI;
import brachy.modularui.drawable.schema.*;
import brachy.modularui.integration.embeddium.SodiumCompat;
import brachy.modularui.utils.FluidTextureType;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class PatternPreviewRenderer {

    public static final PatternPreviewRenderer INSTANCE = new PatternPreviewRenderer();

    private static final Map<RenderLevelStageEvent.Stage, RenderType> STAGE_RENDER_TYPES = Util
            .make(new IdentityHashMap<>(), map -> {
                for (RenderType renderType : RenderType.chunkBufferLayers()) {
                    map.put(RenderLevelStageEvent.Stage.fromRenderType(renderType), renderType);
                }
            });

    private @Nullable MutableSchema schema;
    private @Nullable RenderLevel renderLevel;
    private @Nullable BlockPos controllerPos;
    private final AtomicInteger timeout = new AtomicInteger(-1);

    private RenderCompileTask lastRenderCompileTask = null;
    private final ChunkBufferBuilderPack chunkBufferBuilders = new ChunkBufferBuilderPack();
    private final AtomicReference<CompileStatus> compileStatus = new AtomicReference<>();
    private final AtomicReference<RenderCompileResults> compiledRenderResult = new AtomicReference<>();
    private boolean dirty = true;

    public void showPreview(BlockPos controllerPos, MutableSchema schema, RenderFilter renderFilter, int duration) {
        this.controllerPos = controllerPos;
        this.schema = schema;
        this.renderLevel = new RenderLevel(schema, renderFilter);
        timeout.set(duration);

        notifyRecompile();
    }

    // Layer to show in the render preview, MAX_INT is show all
    private int layers = Integer.MAX_VALUE;

    public void showPreviewCycleLevel(BlockPos controllerPos, MutableSchema schema, int duration) {
        if (controllerPos.equals(this.controllerPos)) {
            var bounds = schema.getBounds();
            int min = bounds.getFirst().getY();
            int max = bounds.getSecond().getY();
            if (layers == Integer.MAX_VALUE) {
                layers = min;
            } else {
                layers += 1;
                if (layers > max) {
                    layers = Integer.MAX_VALUE;
                }
            }
        } else {
            layers = Integer.MAX_VALUE;
        }
        RenderFilter renderFilter;
        if (layers == Integer.MAX_VALUE) {
            renderFilter = RenderFilter.ALL;
        } else {
            renderFilter = (pos, state) -> pos.getY() == layers;
        }
        showPreview(controllerPos, schema, renderFilter, duration);
    }

    public void clientTick() {
        if (timeout.get() > 0 && timeout.decrementAndGet() <= 0) {
            dispose();
        }
    }

    public void draw(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, Camera camera,
                     RenderLevelStageEvent.Stage stage, float partialTick) {
        if (timeout.get() <= 0) return;
        if (this.schema == null || this.controllerPos == null) return;
        if (!camera.isInitialized()) return;

        RenderType renderType = STAGE_RENDER_TYPES.get(stage);
        if (renderType == null && stage != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;

        poseStack.pushPose();
        poseStack.translate(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ());

        Vec3 cameraPos = camera.getPosition();
        if (renderType != null) {
            // render the appropriate chunk layer if renderType is a chunk render layer
            renderBlocks(renderType, poseStack, cameraPos);
        } else {
            // render block entities if renderType==null
            renderBlockEntities(poseStack, bufferSource, partialTick, cameraPos);
        }

        poseStack.popPose();
    }

    public void notifyRecompile() {
        this.dirty = true;
    }

    protected void cancelCompilation() {
        if (this.lastRenderCompileTask != null) {
            this.lastRenderCompileTask.cancel();
            this.lastRenderCompileTask = null;
        }
    }

    private boolean shouldDiscard(CompileStatus status) {
        return status == CompileStatus.CANCELED;
    }

    /// only called from {@link #checkRecompile(Vec3) checkRecompile} when {@linkplain #compileStatus} is CANCELED
    protected void recompile(final Vec3 cameraPos) {
        cancelCompilation();

        this.lastRenderCompileTask = new RenderCompileTask();
        this.compileStatus.set(CompileStatus.COMPILING);

        RenderCompileResults compileResults = new RenderCompileResults();
        CompletableFuture.supplyAsync(
                Util.wrapThreadWithTaskName("preview_chunk_rebuild",
                        () -> this.lastRenderCompileTask.compileBlockBuffers(compileResults, cameraPos)),
                Util.backgroundExecutor())
                .thenCompose(Function.identity())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(error, "Batching chunks"));
                    } else {
                        CompileStatus status = result.status;
                        if (shouldDiscard(status)) {
                            this.chunkBufferBuilders.discardAll();
                        } else {
                            this.chunkBufferBuilders.clearAll();
                        }
                        if (status == CompileStatus.SUCCESS) {
                            if (this.compiledRenderResult.get() != null) {
                                this.compiledRenderResult.get().clearBuffers();
                            }
                            this.compiledRenderResult.set(result);
                        }
                        this.compileStatus.set(status);
                    }
                });
    }

    public void dispose() {
        cancelCompilation();
        if (this.compiledRenderResult.get() != null) {
            this.compiledRenderResult.get().clearBuffers();
            this.compiledRenderResult.set(null);
        }
        this.chunkBufferBuilders.discardAll();
        this.compileStatus.set(CompileStatus.CANCELED);

        this.schema = null;
        this.controllerPos = null;
        this.timeout.set(-1);

        this.dirty = false;
    }

    /// called each draw tick
    private RenderCompileResults checkRecompile(Vec3 cameraPos) {
        CompileStatus status = this.compileStatus.get();
        RenderCompileResults results = this.compiledRenderResult.get();

        // otherwise, check if we're dirty
        // the only possible statuses are CANCELED or SUCCESS
        if (status != CompileStatus.COMPILING && (status == CompileStatus.CANCELED || this.dirty)) {
            this.dirty = false;
            recompile(cameraPos);
        }

        // if we're still compiling, send previous result
        return results;
    }

    protected void renderBlocks(RenderType renderType, PoseStack poseStack, Vec3 cameraPos) {
        RenderCompileResults compileResults = checkRecompile(cameraPos);
        if (compileResults == null) return;

        renderType.setupRenderState();
        ModelBlockRenderer.enableCaching();

        // set up shader uniforms
        ShaderInstance shader = RenderSystem.getShader();
        assert shader != null;

        for (int i = 0; i < GlStateManager.TEXTURE_COUNT; ++i) {
            int textureId = RenderSystem.getShaderTexture(i);
            shader.setSampler("Sampler" + i, textureId);
        }

        if (shader.MODEL_VIEW_MATRIX != null) {
            shader.MODEL_VIEW_MATRIX.set(poseStack.last().pose());
        }
        if (shader.PROJECTION_MATRIX != null) {
            shader.PROJECTION_MATRIX.set(RenderSystem.getProjectionMatrix());
        }
        if (shader.INVERSE_VIEW_ROTATION_MATRIX != null) {
            shader.INVERSE_VIEW_ROTATION_MATRIX.set(RenderSystem.getInverseViewRotationMatrix());
        }
        if (shader.COLOR_MODULATOR != null) {
            shader.COLOR_MODULATOR.set(RenderSystem.getShaderColor());
        }
        if (shader.GLINT_ALPHA != null) {
            shader.GLINT_ALPHA.set(RenderSystem.getShaderGlintAlpha());
        }
        if (shader.FOG_START != null) {
            shader.FOG_START.set(RenderSystem.getShaderFogStart());
        }
        if (shader.FOG_END != null) {
            shader.FOG_END.set(RenderSystem.getShaderFogEnd());
        }
        if (shader.FOG_COLOR != null) {
            shader.FOG_COLOR.set(RenderSystem.getShaderFogColor());
        }
        if (shader.FOG_SHAPE != null) {
            shader.FOG_SHAPE.set(RenderSystem.getShaderFogShape().getIndex());
        }
        if (shader.TEXTURE_MATRIX != null) {
            shader.TEXTURE_MATRIX.set(RenderSystem.getTextureMatrix());
        }
        if (shader.GAME_TIME != null) {
            shader.GAME_TIME.set(RenderSystem.getShaderGameTime());
        }

        if (shader.CHUNK_OFFSET != null) {
            shader.CHUNK_OFFSET.set((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z);
        }

        RenderSystem.setupShaderLights(shader);
        shader.apply();

        // actually draw the chunk
        if (!compileResults.isEmpty(renderType)) {
            if (ModularUI.Mods.isSodiumLikeLoaded()) {
                SodiumCompat.markSpritesAsActive(compileResults.activeFluidSprites);
            }

            VertexBuffer vertexBuffer = compileResults.getOrCreateChunkBuffers().get(renderType);
            // check if the buffer is invalid in case someone breaks it
            // noinspection ConstantValue
            if (vertexBuffer.isInvalid() || vertexBuffer.getFormat() == null) return;

            vertexBuffer.bind();
            vertexBuffer.draw();
        }

        shader.clear();
        VertexBuffer.unbind();
        renderType.clearRenderState();
    }

    protected void renderBlockEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource,
                                       float partialTick, Vec3 cameraPos) {
        RenderCompileResults compileResults = checkRecompile(cameraPos);
        if (compileResults == null) return;

        for (BlockEntity blockEntity : compileResults.blockEntities) {
            if (blockEntity == null) {
                continue;
            }
            BlockPos pos = blockEntity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(pos.getX() - cameraPos.x, pos.getY() - cameraPos.y, pos.getZ() - cameraPos.z);

            Minecraft.getInstance().getBlockEntityRenderDispatcher()
                    .render(blockEntity, partialTick, poseStack, bufferSource);

            poseStack.popPose();
        }

        bufferSource.endBatch(RenderType.solid());
        bufferSource.endBatch(RenderType.endPortal());
        bufferSource.endBatch(RenderType.endGateway());
        bufferSource.endBatch(Sheets.solidBlockSheet());
        bufferSource.endBatch(Sheets.cutoutBlockSheet());
        bufferSource.endBatch(Sheets.bedSheet());
        bufferSource.endBatch(Sheets.shulkerBoxSheet());
        bufferSource.endBatch(Sheets.signSheet());
        bufferSource.endBatch(Sheets.hangingSignSheet());
        bufferSource.endBatch(Sheets.chestSheet());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PatternPreviewRenderer that)) return false;

        return Objects.equals(this.schema, that.schema);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.schema);
    }

    protected enum CompileStatus {
        COMPILING,
        SUCCESS,
        CANCELED
    }

    protected class RenderCompileTask {

        private final AtomicBoolean isCanceled = new AtomicBoolean(false);

        public void cancel() {
            this.isCanceled.set(true);
        }

        protected CompletableFuture<RenderCompileResults> compileBlockBuffers(RenderCompileResults compileResults,
                                                                              Vec3 cameraPos) {
            if (this.isCanceled.get()) {
                return CompletableFuture.completedFuture(compileResults.withStatus(CompileStatus.CANCELED));
            }
            if (PatternPreviewRenderer.this.schema == null || PatternPreviewRenderer.this.renderLevel == null) {
                return CompletableFuture.completedFuture(compileResults.withStatus(CompileStatus.CANCELED));
            }
            RenderLevel fakeLevel = PatternPreviewRenderer.this.renderLevel;

            var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            ChunkBufferBuilderPack chunkBufferBuilders = PatternPreviewRenderer.this.chunkBufferBuilders;

            RandomSource randomSource = RandomSource.create();
            PoseStack poseStack = new PoseStack();
            Set<RenderType> startedBuffers = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());

            ModelBlockRenderer.enableCaching();
            for (var blockEntry : PatternPreviewRenderer.this.schema) {
                BlockPos pos = blockEntry.getKey();
                BlockState blockState = fakeLevel.getBlockState(pos);
                if (blockState.isAir()) continue;
                FluidState fluidState = blockState.getFluidState();

                if (blockState.hasBlockEntity()) {
                    BlockEntity blockEntity = fakeLevel.getBlockEntity(pos);
                    if (blockEntity != null) {
                        compileResults.blockEntities.add(blockEntity);
                    }
                }

                if (!fluidState.isEmpty()) {
                    RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                    BufferBuilder builder = chunkBufferBuilders.builder(renderType);
                    if (startedBuffers.add(renderType)) {
                        if (builder.building()) {
                            GTCEu.LOGGER.warn("Buffer is already building for RenderType: {}!", renderType);
                            return CompletableFuture.completedFuture(compileResults.withStatus(CompileStatus.CANCELED));
                        }
                        builder.begin(renderType.mode(), renderType.format());
                    }

                    SectionPos sectionPos = SectionPos.of(pos);
                    VertexConsumer vertexConsumer = new LiquidVertexConsumer(builder, sectionPos);
                    blockRenderDispatcher.renderLiquid(pos, fakeLevel, vertexConsumer,
                            blockState, fluidState);

                    markFluidSpritesActive(compileResults, fluidState);
                }

                if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                    BakedModel model = blockRenderDispatcher.getBlockModel(blockState);

                    BlockEntity blockEntity = fakeLevel.getBlockEntity(pos);
                    ModelData modelData = ModelData.EMPTY;
                    if (blockEntity != null) {
                        modelData = blockEntity.getModelData();
                    }
                    modelData = model.getModelData(fakeLevel, pos, blockState, modelData);

                    randomSource.setSeed(blockState.getSeed(pos));

                    for (RenderType renderType : model.getRenderTypes(blockState, randomSource, modelData)) {
                        BufferBuilder builder = chunkBufferBuilders.builder(renderType);
                        if (startedBuffers.add(renderType)) {
                            builder.begin(renderType.mode(), renderType.format());
                        }

                        poseStack.pushPose();
                        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

                        // scale the rendered model down by a bit
                        poseStack.translate(0.5f, 0.5f, 0.5f);
                        poseStack.scale(0.8f, 0.8f, 0.8f);
                        poseStack.translate(-0.5f, -0.5f, -0.5f);

                        // disable culling as all sides will be visible (they're scaled down)
                        blockRenderDispatcher.renderBatched(blockState, pos, fakeLevel, poseStack, builder, false,
                                randomSource, modelData, renderType);
                        poseStack.popPose();
                    }
                }
            }

            if (startedBuffers.contains(RenderType.translucent())) {
                BufferBuilder bufferBuilder = chunkBufferBuilders.builder(RenderType.translucent());
                if (!bufferBuilder.isCurrentBatchEmpty()) {
                    bufferBuilder.setQuadSorting(
                            VertexSorting.byDistance((float) cameraPos.x, (float) cameraPos.y, (float) cameraPos.z));
                }
            }

            for (RenderType renderType : startedBuffers) {
                BufferBuilder.RenderedBuffer renderedBuffer = chunkBufferBuilders.builder(renderType)
                        .endOrDiscardIfEmpty();
                if (renderedBuffer != null) {
                    compileResults.renderedLayers.put(renderType, renderedBuffer);
                }
            }
            ModelBlockRenderer.clearCache();

            if (this.isCanceled.get()) {
                compileResults.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                return CompletableFuture.completedFuture(compileResults.withStatus(CompileStatus.CANCELED));
            }

            List<CompletableFuture<Void>> uploads = new ArrayList<>();
            compileResults.renderedLayers.forEach((renderType, buffer) -> {
                uploads.add(uploadChunkLayer(compileResults, buffer, renderType));
                compileResults.hasBlocks.add(renderType);
            });
            return Util.sequenceFailFast(uploads).handle((result, error) -> {
                if (error != null && !(error instanceof CancellationException) &&
                        !(error instanceof InterruptedException)) {
                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(error, "Rendering chunk"));
                }
                if (this.isCanceled.get()) {
                    return compileResults.withStatus(CompileStatus.CANCELED);
                } else {
                    return compileResults.withStatus(CompileStatus.SUCCESS);
                }
            });
        }

        protected CompletableFuture<Void> uploadChunkLayer(RenderCompileResults results,
                                                           BufferBuilder.RenderedBuffer builder,
                                                           RenderType renderType) {
            return CompletableFuture.runAsync(() -> {
                VertexBuffer buffer = results.getOrCreateChunkBuffers().get(renderType);
                if (!buffer.isInvalid()) {
                    buffer.bind();
                    buffer.upload(builder);
                    VertexBuffer.unbind();
                }
            }, runnable -> RenderSystem.recordRenderCall(runnable::run));
        }

        protected static void markFluidSpritesActive(RenderCompileResults compileResults, FluidState fluidState) {
            // For Sodium compatibility, ensure the sprites actually animate
            // even if no block is on-screen that would cause them to otherwise.
            var props = IClientFluidTypeExtensions.of(fluidState);
            compileResults.activeFluidSprites.add(FluidTextureType.STILL.map(props));
            compileResults.activeFluidSprites.add(FluidTextureType.FLOWING.map(props));
        }
    }

    protected static class RenderCompileResults {

        protected CompileStatus status = CompileStatus.COMPILING;
        protected final List<BlockEntity> blockEntities = new ArrayList<>();
        protected final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
        protected final Set<TextureAtlasSprite> activeFluidSprites = new HashSet<>();
        protected final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
        private Map<RenderType, VertexBuffer> chunkBuffers;

        protected @NotNull Map<RenderType, VertexBuffer> getOrCreateChunkBuffers() {
            if (this.chunkBuffers == null || this.chunkBuffers.isEmpty()) {
                List<RenderType> chunkRenderTypes = RenderType.chunkBufferLayers();
                this.chunkBuffers = new Reference2ObjectLinkedOpenHashMap<>();
                for (RenderType type : chunkRenderTypes) {
                    this.chunkBuffers.put(type, new VertexBuffer(VertexBuffer.Usage.STATIC));
                }
            }
            return this.chunkBuffers;
        }

        protected void clearBuffers() {
            if (this.chunkBuffers != null && !this.chunkBuffers.isEmpty()) {
                this.chunkBuffers.values().forEach(VertexBuffer::close);
                this.chunkBuffers.clear();
            }
        }

        public boolean isEmpty(RenderType renderType) {
            return !this.hasBlocks.contains(renderType);
        }

        public RenderCompileResults withStatus(CompileStatus status) {
            this.status = status;
            return this;
        }
    }
}
