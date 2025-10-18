package com.gregtechceu.gtceu.client.mui.schemarenderer;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.schema.ISchema;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widgets.SchemaWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
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

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

/**
 * World rendering is based on Applied energistics 2's <a href=
 * "https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/643dfe2e7e16dac48192d85305d35e2e74a64fb0/src/main/java/appeng/client/guidebook/scene/GuidebookLevelRenderer.java">GuidebookLevelRenderer</a>
 * (LGPLv3)
 */
@Accessors(fluent = true)
public class BaseSchemaRenderer implements IDrawable {

    @Getter
    private final ISchema schema;
    private final RenderLevel renderLevel;
    @Getter
    private final Camera camera = new Camera();
    private final int[] viewport = { 0, 0, 0, 0 };
    @Getter
    private @Nullable BlockHitResult lastRayTrace = null;

    private final DummyLightTexture lightTexture = new DummyLightTexture();

    public BaseSchemaRenderer(ISchema schema) {
        this.schema = schema;
        this.renderLevel = new RenderLevel(schema);
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
        render(context, x, y, width, height, context.getMouseX(), context.getMouseY());
    }

    public void render(GuiContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
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
        renderWorld(context.getGraphics().bufferSource());

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

    /**
     * Raytraces at the given mouse pos.
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

    public void renderWorld(MultiBufferSource.BufferSource bufferSource) {
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
            renderBlocks(bufferSource, false);
            renderBlockEntities(bufferSource);

            // The order comes from LevelRenderer#renderLevel
            bufferSource.endBatch(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS));
            bufferSource.endBatch(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
            bufferSource.endBatch(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
            bufferSource.endBatch(RenderType.entitySmoothCutout(InventoryMenu.BLOCK_ATLAS));

            // These would normally be pre-baked, but they are not for us
            for (RenderType type : RenderType.chunkBufferLayers()) {
                if (type != RenderType.translucent()) {
                    bufferSource.endBatch(type);
                }
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

            renderBlocks(bufferSource, true);
            bufferSource.endBatch(RenderType.translucent());
        });
        RenderSystem.enableDepthTest();
    }

    private void renderBlocks(MultiBufferSource bufferSource, boolean translucent) {
        RandomSource randomSource = RandomSource.createNewThreadLocalInstance();

        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
        PoseStack poseStack = new PoseStack();

        this.schema.iterator().forEachRemaining(entry -> {
            BlockPos pos = entry.getKey();
            BlockState blockState = entry.getValue();
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.isEmpty()) {
                RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
                if (renderType != RenderType.translucent() || translucent) {
                    VertexConsumer bufferBuilder = bufferSource.getBuffer(renderType);

                    SectionPos sectionPos = SectionPos.of(pos);
                    VertexConsumer vertexConsumer = new LiquidVertexConsumer(bufferBuilder, sectionPos);
                    blockRenderDispatcher.renderLiquid(pos, this.renderLevel, vertexConsumer,
                            blockState, fluidState);

                    markFluidSpritesActive(fluidState);
                }
            }

            if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
                BlockEntity be = this.renderLevel.getBlockEntity(pos);
                ModelData modelData = ModelData.EMPTY;
                if (be != null) {
                    modelData = be.getModelData();
                }

                BakedModel model = blockRenderDispatcher.getBlockModel(blockState);
                var renderTypes = model.getRenderTypes(blockState, randomSource, modelData);

                for (RenderType renderType : renderTypes) {
                    if (renderType != RenderType.translucent() || translucent) {
                        VertexConsumer bufferBuilder = bufferSource.getBuffer(renderType);

                        poseStack.pushPose();
                        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
                        blockRenderDispatcher.renderBatched(blockState, pos, this.renderLevel, poseStack,
                                bufferBuilder, true,
                                randomSource, modelData, renderType);
                        poseStack.popPose();
                    }
                }
            }
        });
    }

    private void renderBlockEntities(MultiBufferSource buffers) {
        PoseStack poseStack = new PoseStack();

        this.schema.iterator().forEachRemaining(entry -> {
            BlockPos pos = entry.getKey();
            BlockState state = entry.getValue();
            if (state.hasBlockEntity()) {
                BlockEntity blockEntity = renderLevel.getBlockEntity(pos);
                if (blockEntity != null) {
                    this.handleBlockEntity(poseStack, buffers, blockEntity);
                }
            }
        });
    }

    private static void markFluidSpritesActive(FluidState fluidState) {
        // For Sodium compatibility, ensure the sprites actually animate
        // even if no block is on-screen that would cause them to, otherwise.
        var props = IClientFluidTypeExtensions.of(fluidState);
        Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(props.getStillTexture());
        Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(props.getFlowingTexture());
    }

    private <E extends BlockEntity> void handleBlockEntity(PoseStack poseStack, MultiBufferSource bufferSource,
                                                           E blockEntity) {
        var dispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
        var renderer = dispatcher.getRenderer(blockEntity);
        if (renderer == null) {
            return;
        }
        BlockPos pos = blockEntity.getBlockPos();
        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        int packedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos());
        renderer.render(blockEntity, 0, poseStack, bufferSource, packedLight, OverlayTexture.NO_OVERLAY);
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
     * Converts a relative screen pos to a world pos.
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
}
