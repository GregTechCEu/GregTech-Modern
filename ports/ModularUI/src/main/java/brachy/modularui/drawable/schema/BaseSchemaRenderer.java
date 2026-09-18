package brachy.modularui.drawable.schema;

import brachy.modularui.ModularUI;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.drawable.Icon;
import brachy.modularui.integration.embeddium.SodiumCompat;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.FluidTextureType;
import brachy.modularui.utils.MatrixUtils;
import brachy.modularui.widget.sizer.Area;
import brachy.modularui.widgets.SchemaWidget;

import net.minecraft.CrashReport;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.Sheets;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    @Getter private final ISchema schema;
    private final RenderLevel renderLevel;
    private final Viewport viewport = new Viewport();
    @Getter private final Camera camera = new Camera();
    @Getter private BlockHitResult lastRayTrace = null;
    @Getter private RenderFilter renderFilter = RenderFilter.ALL;

    private RenderCompileTask lastRenderCompileTask = null;
    private final SectionBufferBuilderPack sectionBufferBuilders;
    private final AtomicReference<CompileStatus> compileStatus = new AtomicReference<>(CompileStatus.CANCELED);
    private final AtomicReference<RenderCompileResults> compiledRenderResult = new AtomicReference<>();
    private boolean dirty = true;

    // projection * model view matrix
    @Getter private final Matrix4f projection = new Matrix4f();
    @Getter @Setter private boolean captureDebugInfo;
    @Getter private final Vector3f openGLMousePos = new Vector3f();
    private final List<Vector3f> pos = new ArrayList<>();

    public BaseSchemaRenderer(ISchema schema) {
        this.schema = schema;
        this.renderLevel = new RenderLevel(schema, (pos, state) -> this.renderFilter.shouldRender(pos, state));
        this.sectionBufferBuilders = new SectionBufferBuilderPack();
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

    public boolean isCompiling() {
        return this.compileStatus.get() == CompileStatus.COMPILING;
    }

    public boolean isCompleted() {
        return this.compileStatus.get() == CompileStatus.SUCCESS;
    }

    public boolean isCanceled() {
        return this.compileStatus.get() == CompileStatus.CANCELED;
    }

    private boolean shouldDiscard(CompileStatus status) {
        return status == CompileStatus.CANCELED || status == CompileStatus.DISABLED;
    }

    ///  only called from {@link #checkRecompile()} when {@linkplain #compileStatus} is CANCELED
    protected void recompile() {
        cancelCompilation();

        this.lastRenderCompileTask = new RenderCompileTask();
        this.compileStatus.set(CompileStatus.COMPILING);

        RenderCompileResults compileResults = new RenderCompileResults();
        CompletableFuture.supplyAsync(
                        Util.wrapThreadWithTaskName("scm_chk_rebuild", () -> this.lastRenderCompileTask.compileBlockBuffers(compileResults)),
                        Util.backgroundExecutor())
                .thenCompose(Function.identity())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        Minecraft.getInstance().delayCrash(CrashReport.forThrowable(error, "Batching chunks"));
                    } else {
                        CompileStatus status = result.status;
                        if (shouldDiscard(status)) {
                            this.sectionBufferBuilders.discardAll();
                        } else {
                            this.sectionBufferBuilders.clearAll();
                        }
                        if (status == CompileStatus.SUCCESS) {
                            if (this.compiledRenderResult.get() != null) {
                                this.compiledRenderResult.get().clearBuffer();
                            }
                            this.compiledRenderResult.set(result);
                        }
                        this.compileStatus.set(status);
                        onRendered();
                    }
                });
    }

    public void dispose() {
        cancelCompilation();
        if (this.compiledRenderResult.get() != null) {
            this.compiledRenderResult.get().clearBuffer();
            this.compiledRenderResult.set(null);
        }
        this.compileStatus.set(CompileStatus.DISABLED);
        this.sectionBufferBuilders.discardAll();
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

        Area area = context.getScreenArea();
        int transformX = context.transformX(x, y) + area.x();
        int transformY = context.transformY(x, y) + area.y();
        this.viewport.calculateOpenGLViewportFromRectangle(transformX, transformY, width, height);
        this.viewport.applyViewport();

        onSetupCamera();
        setupCamera(width, height);
        renderWorld(context.getGraphics().bufferSource(), context.getRenderPartialTicks());

        if (doRayTrace() || captureDebugInfo()) {
            BlockHitResult result = null;
            if (Area.isInside(x, y, width, height, mouseX, mouseY)) {
                result = rayTrace(mouseX, mouseY, width, height);
            }
            if (result == null || result.getType() != HitResult.Type.BLOCK) {
                if (this.lastRayTrace != null) {
                    onRayTraceFailed();
                }
            } else {
                onSuccessfulRayTrace(createWorldRenderPose(), result);
            }
            this.lastRayTrace = result;
        }

        resetCamera();
        context.graphicsPose().popPose();

        if (this.captureDebugInfo) {
            drawProjectedBlockPos(context.getGraphics(), width, height);
        }
    }

    public void drawProjectedBlockPos(GuiGraphicsExtractor graphics, int width, int height) {
        for (Vector3f s : this.pos) {
            float x0 = this.viewport.unscaleXFromViewport(s.x, width);
            float y0 = this.viewport.unscaleYFromViewport(s.y, height);
            GuiDraw.drawRect(graphics, x0 - 1, y0 - 1, 2, 2, Color.withAlpha(Color.BLUE.main, 1f));
        }
    }

    public void drawBlockOutlines(MultiBufferSource.BufferSource bufferSource) {
        VertexConsumer vc = bufferSource.getBuffer(RenderType.lines());
        var ps = createWorldRenderPose();
        for (var e : this.schema) {
            if (!e.getValue().isAir()) {
                var p = e.getKey();
                LevelRenderer.renderLineBox(
                        ps,
                        vc,
                        p.getX(), p.getY(), p.getZ(),
                        p.getX() + 1, p.getY() + 1, p.getZ() + 1,
                        1f, 0f, 0f, 1f
                );
            }
        }
        bufferSource.endBatch(RenderType.lines());
    }

    public PoseStack createWorldRenderPose() {
        var ps = new PoseStack();
        ps.translate(-camera.pos().x, -camera.pos().y, -camera.pos().z);
        return ps;
    }

    ///  called each draw tick
    private RenderCompileResults checkRecompile() {
        var status = this.compileStatus.get();
        if (status == CompileStatus.DISABLED) return null; // disabled, no-op

        var res = this.compiledRenderResult.get();

        // otherwise, check if we're dirty
        // the only possible statuses is CANCELED or SUCCESS
        if (status != CompileStatus.COMPILING && (status == CompileStatus.CANCELED || this.dirty)) {
            this.dirty = false;
            recompile();
        }

        // if we're still compiling, send previous result
        return res;

    }

    @SuppressWarnings("deprecation")
    public void renderWorld(MultiBufferSource.BufferSource bufferSource, float partialTick) {
        LevelLightEngine lightEngine = this.renderLevel.getLightEngine();
        while (lightEngine.hasLightWork()) {
            lightEngine.runLightUpdates();
        }

        var renderResult = checkRecompile();
        if (renderResult == null) return;

        // note: this is the live array so we clone it
        float[] prevFogColor = RenderSystem.getShaderFogColor().clone();
        float prevFogStart = RenderSystem.getShaderFogStart();
        float prevFogEnd = RenderSystem.getShaderFogEnd();
        FogShape prevFogShape = RenderSystem.getShaderFogShape();

        // Essentially disable level fog
        RenderSystem.setShaderFogColor(1, 1, 1, 0);
        RenderSystem.setShaderFogStart(0);
        RenderSystem.setShaderFogEnd(1000);
        RenderSystem.setShaderFogShape(FogShape.SPHERE);

        lightTexture.update(this.renderLevel);

        Lighting.setupLevel();

        RenderSystem.disableDepthTest();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.runAsFancy(() -> {
            // The order comes from LevelRenderer#renderLevel

            renderBlocks(renderResult, RenderType.solid());
            // NEO: fix flickering leaves when mods mess up the blurMipmap settings
            Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS)
                    .setBlurMipmap(false, Minecraft.getInstance().options.mipmapLevels().get() > 0);
            renderBlocks(renderResult, RenderType.cutoutMipped());
            Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS).restoreLastBlurMipmap();
            renderBlocks(renderResult, RenderType.cutout());

            bufferSource.endBatch(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
            bufferSource.endBatch(RenderType.entityCutout(TextureAtlas.LOCATION_BLOCKS));
            bufferSource.endBatch(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));
            bufferSource.endBatch(RenderType.entitySmoothCutout(TextureAtlas.LOCATION_BLOCKS));

            if (isBEREnabled()) {
                renderBlockEntities(renderResult, bufferSource, partialTick);
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

            renderBlocks(renderResult, RenderType.translucent());
            renderBlocks(renderResult, RenderType.tripwire());

            if (this.captureDebugInfo) {
                drawBlockOutlines(bufferSource);
            }
        });
        RenderSystem.enableDepthTest();

        Lighting.setupFor3DItems();

        RenderSystem.setShaderFogColor(prevFogColor[0], prevFogColor[1], prevFogColor[2], prevFogColor[3]);
        RenderSystem.setShaderFogStart(prevFogStart);
        RenderSystem.setShaderFogEnd(prevFogEnd);
        RenderSystem.setShaderFogShape(prevFogShape);
    }

    protected void renderBlocks(RenderCompileResults renderResult, RenderType renderType) {
        if (renderResult.isEmpty(renderType)) {
            return;
        }
        VertexBuffer vertexBuffer = renderResult.getOrCreateChunkBuffers().get(renderType);
        // check if the buffer is invalid in case someone breaks it
        // noinspection ConstantValue
        if (vertexBuffer.isInvalid() || vertexBuffer.getFormat() == null) {
            return;
        }

        renderType.setupRenderState();

        // set up shader uniforms
        ShaderInstance shader = RenderSystem.getShader();
        assert shader != null;

        shader.setDefaultUniforms(renderType.mode(),RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix(),
                Minecraft.getInstance().getWindow());
        if (shader.CHUNK_OFFSET != null) {
            shader.CHUNK_OFFSET.set(-camera.pos().x, -camera.pos().y, -camera.pos().z);
        }
        shader.apply();

        // actually draw the chunk
        if (ModularUI.Mods.isSodiumLikeLoaded()) {
            SodiumCompat.markSpritesAsActive(renderResult.activeFluidSprites);
        }

        vertexBuffer.bind();
        vertexBuffer.draw();

        if (shader.CHUNK_OFFSET != null) {
            shader.CHUNK_OFFSET.set(0f, 0f, 0f);
        }

        shader.clear();
        VertexBuffer.unbind();
        renderType.clearRenderState();
    }

    protected void renderBlockEntities(RenderCompileResults renderResult, MultiBufferSource bufferSource, float partialTick) {
        PoseStack poseStack = new PoseStack();

        for (BlockEntity blockEntity : renderResult.blockEntities) {
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
        RenderSystem.clearColor(Color.getRedF(clearColor), Color.getGreenF(clearColor), Color.getBlueF(clearColor), Color.getAlphaF(clearColor));
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
        Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushMatrix();
        modelViewStack.identity();

        if (isIsometric()) {
            // see GameRenderer:935
            // Vanilla uses a -2000 z translation for isometric rendering
            modelViewStack.translate(0.0f, 0.0f, -2000.0f);
        }
        MatrixUtils.lookAt(modelViewStack, this.camera.pos(), this.camera.lookAt());
        RenderSystem.applyModelViewMatrix();

        rebuildProjection(); // rebuild projection matrix
    }

    protected void resetCamera() {
        // reset viewport
        Window window = Minecraft.getInstance().getWindow();
        RenderSystem.viewport(0, 0, window.getWidth(), window.getHeight());

        // restore projection matrix
        RenderSystem.restoreProjectionMatrix();

        // restore model view matrix
        RenderSystem.getModelViewStack().popMatrix();
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
        var m = projection();
        if (this.captureDebugInfo) {
            int i = 0;
            for (var e : this.schema) {
                if (!e.getValue().isAir()) {
                    var p = e.getKey();
                    boolean reuseVec = this.pos.size() > i;
                    var vec = reuseVec ? this.pos.get(i) : new Vector3f();
                    vec = m.project(p.getX() + 0.5f, p.getY() + 0.5f, p.getZ() + 0.5f, this.viewport.getViewport(), vec);
                    if (reuseVec) this.pos.set(i, vec);
                    else this.pos.add(vec);
                    i++;
                }
            }
            while (i < this.pos.size()) this.pos.remove(i);
        }
        screenToOpenGLPos(mouseX, mouseY, width, height, 1, this.captureDebugInfo, this.openGLMousePos);
        float d = openGLMousePos.z;

        this.openGLMousePos.z = 0;
        Vector3f worldPos = screenToWorldPos(m, this.openGLMousePos);
        this.openGLMousePos.z = 1;
        Vector3f target = screenToWorldPos(m, this.openGLMousePos);
        this.openGLMousePos.z = d;

        ClipContext context = new ClipContext(new Vec3(worldPos), new Vec3(target),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.ANY, CollisionContext.empty());
        return this.renderLevel.clip(context);
    }

    public Vector3f screenToOpenGLPos(int x, int y, int width, int height, Vector3f dest) {
        return screenToOpenGLPos(x, y, width, height, 0, true, dest);
    }

    public Vector3f screenToOpenGLPos(int x, int y, int width, int height, float depth, Vector3f dest) {
        return screenToOpenGLPos(x, y, width, height, depth, false, dest);
    }

    private Vector3f screenToOpenGLPos(int x, int y, int width, int height, float depth, boolean readDepth, Vector3f dest) {
        this.viewport.rescaleToViewport(x, y, width, height, dest);
        if (readDepth) depth = MatrixUtils.readDepth((int) dest.x, (int) dest.y);
        dest.z = depth;
        return dest;
    }

    public Vector3f screenToWorldPos(int x, int y, int screenWidth, int screenHeight) {
        return screenToWorldPos(projection(), screenToOpenGLPos(x, y, screenWidth, screenHeight, new Vector3f()), new Vector3f());
    }

    private Vector3f screenToWorldPos(Matrix4f projection, Vector3f openGLPos) {
        return screenToWorldPos(projection, openGLPos, new Vector3f());
    }

    private Vector3f screenToWorldPos(Matrix4f projection, Vector3f openGLPos, Vector3f dest) {
        return projection.unproject(openGLPos, this.viewport.getViewport(), dest);
    }

    private void rebuildProjection() {
        this.projection.set(RenderSystem.getModelViewMatrix());
        this.projection.translate(-this.camera.pos().x, -this.camera.pos().y, -this.camera.pos().z);
        RenderSystem.getProjectionMatrix().mul(this.projection, this.projection);
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

    /**
     * Sets a render filter for the block to be rendered. This also causes the world to rerender.
     * This method or {@link #notifyRecompile()} needs to be called everytime the render filter changes.
     */
    public void updateRenderFilter(RenderFilter renderFilter) {
        notifyRecompile();
        this.renderFilter = renderFilter != null ? renderFilter : RenderFilter.ALL;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BaseSchemaRenderer that)) return false;

        return this.schema.equals(that.schema) && this.renderLevel.equals(that.renderLevel) && this.camera.equals(that.camera) &&
                Objects.equals(this.viewport, that.viewport) && Objects.equals(this.renderFilter, that.renderFilter);
    }

    @Override
    public int hashCode() {
        int result = this.schema.hashCode();
        result = 31 * result + this.renderLevel.hashCode();
        result = 31 * result + this.camera.hashCode();
        result = 31 * result + this.viewport.hashCode();
        result = 31 * result + Objects.hashCode(this.renderFilter);
        return result;
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

        protected CompletableFuture<RenderCompileResults> compileBlockBuffers(RenderCompileResults compileResults) {
            if (this.isCanceled.get()) {
                return CompletableFuture.completedFuture(compileResults.withStatus(CompileStatus.CANCELED));
            }

            var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
            SectionBufferBuilderPack sectionBufferBuilders = BaseSchemaRenderer.this.sectionBufferBuilders;

            RandomSource randomSource = RandomSource.create();
            PoseStack poseStack = new PoseStack();
            Map<RenderType, BufferBuilder> bufferLayers = new Reference2ObjectArrayMap<>(RenderType.chunkBufferLayers().size());

            ModelBlockRenderer.enableCaching();
            for (var blockEntry : BaseSchemaRenderer.this.schema) {
                if (!BaseSchemaRenderer.this.renderFilter.shouldRender(blockEntry.getKey(), blockEntry.getValue())) {
                    continue;
                }
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
                    BufferBuilder builder = getOrBeginLayer(bufferLayers, sectionBufferBuilders, renderType);

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
                        BufferBuilder builder = getOrBeginLayer(bufferLayers, sectionBufferBuilders, renderType);

                        poseStack.pushPose();
                        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                        blockRenderDispatcher.renderBatched(blockState, pos, BaseSchemaRenderer.this.renderLevel,
                                poseStack, builder, true, randomSource, modelData, renderType);
                        poseStack.popPose();
                    }
                }
            }

            for (var entry : bufferLayers.entrySet()) {
                RenderType renderType = entry.getKey();
                MeshData meshData = entry.getValue().build();

                if (meshData != null) {
                    if (renderType == RenderType.translucent()) {
                        meshData.sortQuads(sectionBufferBuilders.buffer(RenderType.translucent()),
                                VertexSorting.byDistance(camera.pos()));
                    }
                    compileResults.renderedLayers.put(renderType, meshData);
                }
            }
            ModelBlockRenderer.clearCache();

            if (this.isCanceled.get()) {
                compileResults.renderedLayers.values().forEach(MeshData::close);
                return CompletableFuture.completedFuture(compileResults.withStatus(CompileStatus.CANCELED));
            }

            List<CompletableFuture<Void>> uploads = Lists.newArrayList();
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

        protected BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> bufferLayers,
                                                SectionBufferBuilderPack sectionBufferBuilderPack, RenderType renderType) {
            BufferBuilder builder = bufferLayers.get(renderType);
            if (builder == null) {
                ByteBufferBuilder bytebufferbuilder = sectionBufferBuilderPack.buffer(renderType);
                builder = new BufferBuilder(bytebufferbuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
                bufferLayers.put(renderType, builder);
            }

            return builder;
        }

        protected CompletableFuture<Void> uploadChunkLayer(RenderCompileResults results, MeshData meshData, RenderType renderType) {
            return CompletableFuture.runAsync(() -> {
                VertexBuffer buffer = results.getOrCreateChunkBuffers().get(renderType);
                if (!buffer.isInvalid()) {
                    buffer.bind();
                    buffer.upload(meshData);
                    VertexBuffer.unbind();
                }
            }, runnable -> RenderSystem.recordRenderCall(runnable::run));
        }

        protected static void markFluidSpritesActive(RenderCompileResults compileResults, FluidState fluidState) {
            // For Sodium compatibility, ensure the sprites actually animate
            // even if no block is on-screen that would cause them to, otherwise.
            var props = IClientFluidTypeExtensions.of(fluidState);
            compileResults.activeFluidSprites.add(FluidTextureType.STILL.map(props));
            compileResults.activeFluidSprites.add(FluidTextureType.FLOWING.map(props));
        }
    }

    protected static class RenderCompileResults {

        protected CompileStatus status = CompileStatus.COMPILING;
        protected final List<BlockEntity> blockEntities = new ArrayList<>();
        protected final Map<RenderType, MeshData> renderedLayers = new Reference2ObjectArrayMap<>();
        protected final Set<TextureAtlasSprite> activeFluidSprites = new HashSet<>();
        protected final Set<RenderType> hasBlocks = new ObjectArraySet<>(RenderType.chunkBufferLayers().size());
        private Map<RenderType, VertexBuffer> chunkBuffers = getOrCreateChunkBuffers();

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

        protected void clearBuffer() {
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
