package com.gregtechceu.gtceu.client.mui.schemarenderer;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.schema.ISchema;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widgets.SchemaWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.util.RenderUtil;
import com.gregtechceu.gtceu.integration.embeddium.GTEmbeddiumCompat;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import net.minecraft.CrashReport;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.client.model.data.ModelData;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArraySet;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * World rendering is based on Applied energistics 2's <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/643dfe2e7e16dac48192d85305d35e2e74a64fb0/src/main/java/appeng/client/guidebook/scene/GuidebookLevelRenderer.java">GuidebookLevelRenderer</a>
 * (LGPLv3)
 */
@Accessors(fluent = true)
public class BaseSchemaRenderer implements IDrawable {

    private static final DummyLightTexture lightTexture = new DummyLightTexture();

    @Getter
    private final ISchema schema;
    private final RenderLevel renderLevel;
    private @Nullable RenderCompileTask lastRenderCompileTask = null;
    @Getter
    private final Camera camera = new Camera();
    private final int[] viewport = { 0, 0, 0, 0 };
    @Getter
    private @Nullable BlockHitResult lastRayTrace = null;

    private final ChunkBufferBuilderPack chunkBufferBuilders;

    private final AtomicReference<RenderCompileResults> compileResults = new AtomicReference<>();
    private final AtomicReference<CompileStatus> compileStatus = new AtomicReference<>(CompileStatus.CANCELED);
    private @Nullable Map<RenderType, VertexBuffer> chunkBuffers = getOrCreateChunkBuffers();

    public BaseSchemaRenderer(ISchema schema) {
        this.schema = schema;
        this.renderLevel = new RenderLevel(schema);
        this.chunkBufferBuilders = new ChunkBufferBuilderPack();
    }

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

    public void clearChunkBuffers() {
        if (this.lastRenderCompileTask != null) {
            this.lastRenderCompileTask.cancel();
            this.lastRenderCompileTask = null;
        }
        if (this.chunkBuffers != null && !this.chunkBuffers.isEmpty()) {
            this.chunkBuffers.values().forEach(VertexBuffer::close);
            this.chunkBuffers.clear();
        }
    }

    public void recompile() {
        clearChunkBuffers();
        this.compileStatus.set(CompileStatus.COMPILING);
        this.lastRenderCompileTask = new RenderCompileTask();

        CompletableFuture.supplyAsync(
                Util.wrapThreadWithTaskName("scm_chk_rebuild", this.lastRenderCompileTask::compileBlockBuffers),
                Util.backgroundExecutor())
                .thenCompose(Function.identity())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(error, "Batching chunks"));
                    } else {
                        if (result != CompileStatus.CANCELED && result != CompileStatus.DISABLED) {
                            this.chunkBufferBuilders.clearAll();
                        } else {
                            this.chunkBufferBuilders.discardAll();
                        }
                        this.compileStatus.set(result);
                    }
                });
    }

    public void dispose() {
        clearChunkBuffers();

        this.chunkBufferBuilders.discardAll();
        this.compileStatus.set(CompileStatus.DISABLED);
    }

    @Override
    public SchemaWidget asWidget() {
        return new SchemaWidget(this);
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(50);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        int mouseX = context.getMouseX();
        int mouseY = context.getMouseY();

        context.getGraphics().flush();
        context.graphicsPose().pushPose();
        // context.getStencil().push(x, y, width, height);

        Window window = Minecraft.getInstance().getWindow();
        double guiScale = window.getGuiScale();
        this.viewport[0] = Mth.ceil(context.transformX(x, y) * guiScale);
        this.viewport[1] = window.getHeight() - Mth.ceil((context.transformY(x, y) + height) * guiScale);
        this.viewport[2] = Mth.ceil(width * guiScale);
        this.viewport[3] = Mth.ceil(height * guiScale);

        RenderSystem.viewport(this.viewport[0], this.viewport[1], this.viewport[2], this.viewport[3]);

        onSetupCamera();
        setupCamera(width, height);
        renderWorld(context.getGraphics().bufferSource(), context.getPartialTicks());

        if (doRayTrace()) {
            BlockHitResult result = null;
            if (Area.isInside(x, y, width, height, mouseX, mouseY)) {
                result = rayTrace(mouseX, mouseY, width, height);
            }
            if (result == null || result.getType() != HitResult.Type.BLOCK) {
                if (this.lastRayTrace != null) {
                    onRayTraceFailed();
                }
            } else {
                onSuccessfulRayTrace(context.graphicsPose(), result);
            }
            this.lastRayTrace = result;
        }

        resetCamera();
        context.graphicsPose().popPose();
        // context.getStencil().pop();
    }

    @SuppressWarnings("deprecation")
    public void renderWorld(MultiBufferSource.BufferSource bufferSource, float partialTick) {
        CompileStatus status = this.compileStatus.get();
        if (status == CompileStatus.DISABLED || status == CompileStatus.COMPILING) {
            return;
        } else if (status == CompileStatus.CANCELED) {
            recompile();
            return;
        }

        // Essentially disable level fog
        RenderSystem.setShaderFogColor(1, 1, 1, 0);
        RenderSystem.setShaderFogStart(0);
        RenderSystem.setShaderFogEnd(1000);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);

        lightTexture.update(this.renderLevel);
        LevelLightEngine lightEngine = this.renderLevel.getLightEngine();
        while (lightEngine.hasLightWork()) {
            lightEngine.runLightUpdates();
        }

        Lighting.setupLevel(RenderSystem.getModelViewMatrix());

        RenderSystem.disableDepthTest();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.runAsFancy(() -> {
            // The order comes from LevelRenderer#renderLevel

            renderBlocks(RenderType.solid());
            // FORGE: fix flickering leaves when mods mess up the blurMipmap settings
            Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .setBlurMipmap(false, Minecraft.getInstance().options.mipmapLevels().get() > 0);
            renderBlocks(RenderType.cutoutMipped());
            Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
            renderBlocks(RenderType.cutout());

            bufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
            bufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
            bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
            bufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));

            if (isBEREnabled()) {
                renderBlockEntities(bufferSource, partialTick);
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
            bufferSource.endLastBatch();

            renderBlocks(RenderType.translucent());
            renderBlocks(RenderType.tripwire());
        });
        RenderSystem.enableDepthTest();
    }

    protected void renderBlocks(RenderType renderType) {
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
            shader.MODEL_VIEW_MATRIX.set(RenderSystem.getModelViewMatrix());
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

        if (shader.SCREEN_SIZE != null) {
            Window window = Minecraft.getInstance().getWindow();
            shader.SCREEN_SIZE.set((float) window.getWidth(), (float) window.getHeight());
        }

        if (shader.CHUNK_OFFSET != null) {
            shader.CHUNK_OFFSET.set(-camera.pos().x, -camera.pos().y, -camera.pos().z);
        }

        RenderSystem.setupShaderLights(shader);
        shader.apply();

        // actually draw the chunk
        RenderCompileResults compileResults = this.compileResults.get();
        if (compileResults != null && !compileResults.isEmpty(renderType)) {
            if (GTCEu.Mods.isSodiumRubidiumEmbeddiumLoaded()) {
                GTEmbeddiumCompat.markSpritesAsActive(compileResults.activeFluidSprites);
            }

            VertexBuffer vertexBuffer = getOrCreateChunkBuffers().get(renderType);
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

    protected void renderBlockEntities(MultiBufferSource bufferSource, float partialTick) {
        PoseStack poseStack = new PoseStack();

        RenderCompileResults compileResults = this.compileResults.get();
        if (compileResults == null) {
            return;
        }
        for (BlockEntity blockEntity : compileResults.blockEntities) {
            if (blockEntity != null) {
                this.handleBlockEntity(poseStack, bufferSource, partialTick, blockEntity);
            }
        }
    }

    protected <E extends BlockEntity> void handleBlockEntity(PoseStack poseStack, MultiBufferSource bufferSource,
                                                             float partialTick, E blockEntity) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(pos.getX() - camera.pos().x, pos.getY() - camera.pos().y, pos.getZ() - camera.pos().z);

        // noinspection DataFlowIssue
        int packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());
        renderer.render(blockEntity, partialTick, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    protected void setupCamera(int width, int height) {
        // setup viewport and clear GL buffers
        int clearColor = getClearColor();
        RenderSystem.clearColor(Color.getRedF(clearColor), Color.getGreenF(clearColor), Color.getBlueF(clearColor),
                Color.getAlphaF(clearColor));
        RenderSystem.backupProjectionMatrix();

        float near = 0.05f;
        float far = 10000.0f;
        float fovY = 60.0f * Mth.DEG_TO_RAD; // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        float top = -near * (float) Math.tan(fovY * 0.5);
        float bottom = -top;
        float left = aspect * bottom;
        float right = aspect * top;
        Matrix4f projection = new Matrix4f();
        if (isIsometric()) {
            projection.setOrtho(left, right, bottom, top, near, far);
            RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
        } else {
            projection.setPerspective(fovY, aspect, near, far);
            RenderSystem.setProjectionMatrix(projection, VertexSorting.byDistance(camera.pos()));
        }

        // set up model view matrix
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        if (isIsometric()) {
            // see GameRenderer:935
            // Vanilla uses a -2000 z translation for isometric rendering
            modelViewStack.translate(0.0f, 0.0f, -2000.0f);
        }
        GTMatrixUtils.lookAt(modelViewStack, this.camera.pos(), this.camera.lookAt());

        RenderSystem.applyModelViewMatrix();
    }

    protected void resetCamera() {
        // reset viewport
        Window window = Minecraft.getInstance().getWindow();
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());

        // restore projection matrix
        RenderSystem.restoreProjectionMatrix();

        // restore model view matrix
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();
    }

    /**
     * Traces a ray from the given mouse pos into the rendered scene.
     *
     * @param mouseX A mouse x pos from 0 to width
     * @param mouseY A mouse y pos from 0 to height
     * @param width  Height of the drawn framebuffer
     * @param height Width of the drawn framebuffer
     * @return raytrace result
     */
    protected BlockHitResult rayTrace(int mouseX, int mouseY, int width, int height) {
        // transform mouse pos into relative mouse pos from 0 - 1
        Vector3f worldPos = screenToWorldPos((float) mouseX / width, (float) mouseY / height);
        Vector3f target = this.camera.getLookVec().mul(20).add(worldPos);
        ClipContext context = new ClipContext(new Vec3(worldPos), new Vec3(target), ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY, null);
        return this.renderLevel.clip(context);
    }

    /**
     * Converts a relative screen space position to a world space position in the preview.
     *
     * @param x X pos from 0 to 1
     * @param y Y pos from 0 to 1
     * @return world pos
     */
    protected Vector3f screenToWorldPos(float x, float y) {
        // convert relative pos to framebuffer pos
        int wx = (int) (x * this.viewport[2]);
        int wy = (int) (y * this.viewport[3]);
        return GTMatrixUtils.projectScreenToWorld(wx, wy, this.viewport, true);
    }

    @ApiStatus.OverrideOnly
    protected void onSetupCamera() {}

    @ApiStatus.OverrideOnly
    protected void onRendered() {}

    @ApiStatus.OverrideOnly
    protected void onSuccessfulRayTrace(PoseStack poseStack, @NotNull BlockHitResult result) {}

    @ApiStatus.OverrideOnly
    protected void onRayTraceFailed() {}

    public boolean doRayTrace() {
        return false;
    }

    public int getClearColor() {
        return Color.withAlpha(Color.WHITE.main, 0.5f);
    }

    public boolean isIsometric() {
        return false;
    }

    public boolean isBEREnabled() {
        return true;
    }

    protected enum CompileStatus {
        DISABLED,
        COMPILING,
        SUCCESS,
        CANCELED
    }

    protected class RenderCompileTask {

        private final AtomicBoolean isCanceled = new AtomicBoolean(false);

        public void cancel() {
            this.isCanceled.set(true);
        }

        protected CompletableFuture<CompileStatus> compileBlockBuffers() {
            if (this.isCanceled.get()) {
                return CompletableFuture.completedFuture(CompileStatus.CANCELED);
            }

            var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            ChunkBufferBuilderPack chunkBufferBuilders = BaseSchemaRenderer.this.chunkBufferBuilders;

            RenderCompileResults compileResults = new RenderCompileResults();
            RandomSource randomSource = RandomSource.create();
            PoseStack poseStack = new PoseStack();
            Set<RenderType> startedBuffers = new ReferenceArraySet<>(RenderType.chunkBufferLayers().size());

            ModelBlockRenderer.enableCaching();
            for (var blockEntry : BaseSchemaRenderer.this.schema) {
                BlockPos pos = blockEntry.getKey();
                BlockState blockState = blockEntry.getValue();
                FluidState fluidState = blockState.getFluidState();

                if (blockState.hasBlockEntity()) {
                    BlockEntity blockEntity = BaseSchemaRenderer.this.renderLevel.getBlockEntity(pos);
                    if (blockEntity != null) {
                        compileResults.blockEntities.add(blockEntity);
                    }
                }

                if (!fluidState.isEmpty()) {
                    RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                    BufferBuilder builder = chunkBufferBuilders.builder(renderType);
                    if (startedBuffers.add(renderType)) {
                        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                    }

                    SectionPos sectionPos = SectionPos.of(pos);
                    VertexConsumer vertexConsumer = new LiquidVertexConsumer(builder, sectionPos);
                    blockRenderDispatcher.renderLiquid(pos, BaseSchemaRenderer.this.renderLevel, vertexConsumer,
                            blockState, fluidState);

                    markFluidSpritesActive(compileResults, fluidState);
                }

                if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                    BakedModel model = blockRenderDispatcher.getBlockModel(blockState);

                    BlockEntity blockEntity = BaseSchemaRenderer.this.renderLevel.getBlockEntity(pos);
                    ModelData modelData = ModelData.EMPTY;
                    if (blockEntity != null) {
                        modelData = blockEntity.getModelData();
                    }
                    modelData = model.getModelData(BaseSchemaRenderer.this.renderLevel, pos, blockState, modelData);

                    randomSource.setSeed(blockState.getSeed(pos));

                    for (RenderType renderType : model.getRenderTypes(blockState, randomSource, modelData)) {
                        BufferBuilder builder = chunkBufferBuilders.builder(renderType);
                        if (startedBuffers.add(renderType)) {
                            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                        }

                        poseStack.pushPose();
                        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                        blockRenderDispatcher.renderBatched(blockState, pos, BaseSchemaRenderer.this.renderLevel,
                                poseStack, builder, true, randomSource, modelData, renderType);
                        poseStack.popPose();
                    }
                }
            }

            if (startedBuffers.contains(RenderType.translucent())) {
                BufferBuilder bufferBuilder = chunkBufferBuilders.builder(RenderType.translucent());
                if (!bufferBuilder.isCurrentBatchEmpty()) {
                    bufferBuilder.setQuadSorting(VertexSorting.byDistance(camera.pos()));
                }
            }

            for (RenderType rendertype : startedBuffers) {
                BufferBuilder.RenderedBuffer renderedBuffer = chunkBufferBuilders.builder(rendertype)
                        .endOrDiscardIfEmpty();
                if (renderedBuffer != null) {
                    compileResults.renderedLayers.put(rendertype, renderedBuffer);
                }
            }
            ModelBlockRenderer.clearCache();

            if (this.isCanceled.get()) {
                compileResults.renderedLayers.values().forEach(BufferBuilder.RenderedBuffer::release);
                return CompletableFuture.completedFuture(CompileStatus.CANCELED);
            }

            List<CompletableFuture<Void>> uploads = Lists.newArrayList();
            compileResults.renderedLayers.forEach((renderType, buffer) -> {
                uploads.add(uploadChunkLayer(buffer, renderType));
                compileResults.hasBlocks.add(renderType);
            });
            return Util.sequenceFailFast(uploads).handle((result, error) -> {
                if (error != null && !(error instanceof CancellationException) &&
                        !(error instanceof InterruptedException)) {
                    Minecraft.getInstance().delayCrash(CrashReport.forThrowable(error, "Rendering chunk"));
                }

                if (this.isCanceled.get()) {
                    return CompileStatus.CANCELED;
                } else {
                    BaseSchemaRenderer.this.compileResults.set(compileResults);
                    return CompileStatus.SUCCESS;
                }
            });
        }

        protected CompletableFuture<Void> uploadChunkLayer(BufferBuilder.RenderedBuffer builder,
                                                           RenderType renderType) {
            return CompletableFuture.runAsync(() -> {
                VertexBuffer buffer = getOrCreateChunkBuffers().get(renderType);
                if (!buffer.isInvalid()) {
                    buffer.bind();
                    buffer.upload(builder);
                    VertexBuffer.unbind();
                }
            }, runnable -> RenderSystem.recordRenderCall(runnable::run));
        }

        protected static void markFluidSpritesActive(RenderCompileResults compileResults, FluidState fluidState) {
            // For Sodium compatibility, ensure the sprites actually animate
            // even if no block is on-screen that would cause them to, otherwise.
            var props = IClientFluidTypeExtensions.of(fluidState);
            compileResults.activeFluidSprites.add(RenderUtil.FluidTextureType.STILL.map(props));
            compileResults.activeFluidSprites.add(RenderUtil.FluidTextureType.FLOWING.map(props));
        }
    }

    protected static class RenderCompileResults {

        protected final List<BlockEntity> blockEntities = new ArrayList<>();
        protected final Map<RenderType, BufferBuilder.RenderedBuffer> renderedLayers = new Reference2ObjectArrayMap<>();
        protected final Set<TextureAtlasSprite> activeFluidSprites = new HashSet<>();

        protected final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());

        public boolean isEmpty(RenderType renderType) {
            return !this.hasBlocks.contains(renderType);
        }
    }
}
