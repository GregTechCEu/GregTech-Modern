package com.gregtechceu.gtceu.utils.fakelevel;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.api.mui.widgets.SchemaWidget;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Accessors(fluent = true)
public class BaseSchemaRenderer implements IDrawable {

    private static final RenderTarget FBO = new TextureTarget(1080, 1080, true, Minecraft.ON_OSX);

    @Getter
    private final ISchema schema;
    private final LevelReader renderLevel;
    private final RenderTarget renderTarget;
    @Getter
    private final Camera camera = new Camera();
    @Getter
    private @Nullable BlockHitResult lastRayTrace = null;

    public BaseSchemaRenderer(ISchema schema, RenderTarget renderTarget) {
        this.schema = schema;
        this.renderTarget = renderTarget;
        this.renderLevel = new RenderLevel(schema);
    }

    public BaseSchemaRenderer(ISchema schema) {
        this(schema, FBO);
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
        onSetupCamera();

        this.renderTarget.setClearColor(0.9F, 0.8F, 0.8F, 0.1F);
        this.renderTarget.clear(Minecraft.ON_OSX);
        this.renderTarget.bindWrite(true);

        context.getGraphics().pose().pushPose();
        setupCamera(this.renderTarget.viewWidth, this.renderTarget.viewHeight);
        renderWorld(context);
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
                onSuccessfulRayTrace(context.getGraphics(), result);
            }
            this.lastRayTrace = result;
        }
        resetCamera();
        context.getGraphics().pose().popPose();
        this.renderTarget.unbindWrite();
        Minecraft.getInstance().getMainRenderTarget().bindWrite(true);

        // bind FBO as texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, this.renderTarget.getColorTextureId());
        RenderSystem.setShaderColor(1, 1, 1, 1);

        // render rect with FBO texture
        GuiDraw.drawTexture(context.getGraphics().pose().last().pose(), x, y, x + width, y + height, 0f, 0f, 1f, 1f);
    }

    protected BlockHitResult rayTrace(int mouseX, int mouseY, int width, int height) {
        final float halfPI = (float) (Math.PI / 2);
        Vector3f cameraPos = camera.pos();
        float yaw = camera.yaw();
        float pitch = camera.pitch();

        Vector3f mouseXShift = new Vector3f(1, 0, 0)
                // TODO
                // .rotatePitch(pitch)
                // .rotateYaw(-yaw + halfPI)
                .mul(mouseX - width / 2f)
                .mul(1 / 32f);
        Vector3f mouseYShift = new Vector3f(0, -1, 0)
                // .rotatePitch(pitch)
                // .rotateYaw(-yaw + halfPI)
                .mul(mouseY - height / 2f)
                .mul(1 / 32f);
        Vector3f mousePos = cameraPos.add(mouseXShift, new Vector3f()).add(mouseYShift);
        Vector3f focus = camera.lookAt();
        float perspectiveCompensation = isIsometric() ? 1 : cameraPos.distance(focus) / 3 * width / 100;
        Vector3f underMousePos = focus.add(mouseXShift.mul(perspectiveCompensation), new Vector3f())
                .add(mouseYShift.mul(perspectiveCompensation));
        Vector3f look = underMousePos.sub(mousePos, new Vector3f()).mul(10);
        mousePos.add(look, underMousePos);
        ClipContext context = new ClipContext(new Vec3(mousePos), new Vec3(underMousePos),
                ClipContext.Block.VISUAL, ClipContext.Fluid.ANY, null);
        return schema.getLevel().clip(context);
    }

    private void renderWorld(GuiContext context) {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        RandomSource random = RandomSource.create();

        Minecraft mc = Minecraft.getInstance();
        RenderSystem.enableCull();
        //Lighting.setupForFlatItems();
        Lighting.setupLevel(poseStack.last().pose());
        //mc.gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        List<BlockEntity> tesr = null;
        // render block in each layer
        List<RenderType> chunkBufferLayers = RenderType.chunkBufferLayers();
        for (int i = 0; i < chunkBufferLayers.size(); i++) {
            RenderType layer = chunkBufferLayers.get(i);
            if (i == 0 && isBEREnabled()) {
                tesr = renderBlocksInLayer(poseStack, mc, layer, random, true);
            } else {
                renderBlocksInLayer(poseStack, mc, layer, random, false);
            }

        }

        // render TESR
        if (tesr != null && !tesr.isEmpty()) {
            renderTesr(context.getGraphics(), mc, tesr);
        }

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    private List<BlockEntity> renderBlocksInLayer(PoseStack pose, Minecraft mc, RenderType type, RandomSource random,
                                                  boolean collectTesr) {
        List<BlockEntity> tesr = collectTesr ? new ArrayList<>() : null;
        type.setupRenderState();

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        this.schema.forEach(pair -> {
            BlockPos pos = pair.getKey();
            BlockState state = pair.getValue().getBlockState();
            if (state.getRenderShape() == RenderShape.INVISIBLE) return;
            var be = pair.getValue().getBlockEntity();

            if (collectTesr) {
                if (be != null && !be.isRemoved()) {
                    if (mc.getBlockEntityRenderDispatcher().getRenderer(be) != null) {
                        // only collect tiles to render which actually have a tesr
                        tesr.add(be);
                    }
                }
            }

            ModelData modelData = ModelData.EMPTY;
            if (be != null) {
                modelData = be.getModelData();
            }
            pose.pushPose();
            //pose.setIdentity();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.renderBatched(state, pos, this.renderLevel,
                    pose, buffer, true,
                    random, modelData, type);
            pose.popPose();

        });
        Tesselator.getInstance().end();
        type.clearRenderState();
        return tesr;
    }

    private static void renderTesr(GuiGraphics graphics, Minecraft mc, List<BlockEntity> tileEntities) {
        MultiBufferSource.BufferSource bufferSource = graphics.bufferSource();
        RenderSystem.setShaderColor(1, 1, 1, 1);

        for (Iterator<BlockEntity> iterator = tileEntities.iterator(); iterator.hasNext();) {
            BlockEntity tile = iterator.next();
            if (tile == null || tile.isRemoved()) continue;

            mc.getBlockEntityRenderDispatcher().render(tile, mc.getPartialTick(), graphics.pose(), bufferSource);
        }
        bufferSource.endBatch();
    }

    protected void setupCamera(int width, int height) {
        //Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        // setup viewport and clear GL buffers
        RenderSystem.viewport(0, 0, width, height);
        Color.setGlColor(getClearColor());
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.backupProjectionMatrix();

        float near = isIsometric() ? 1f : 0.1f;
        float far = 10000.0f;
        float fovY = (float) Math.toRadians(60.0f); // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        float top = near * (float) Math.tan(fovY / 2.0);
        float bottom = -top;
        float left = aspect * bottom;
        float right = aspect * top;
        Matrix4f projection = new Matrix4f();
        if (isIsometric()) {
            projection.setOrtho(left, right, bottom, top, near, far);
            RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
        } else {
            projection.setFrustum(left, right, bottom, top, near, far);
            RenderSystem.setProjectionMatrix(projection, VertexSorting.byDistance(camera.pos()));
        }

        // setup modelview matrix
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        if (isIsometric()) {
            modelViewStack.scale(0.1f, 0.1f, 0.1f);
        } else {
            //modelViewStack.scale(-1f, -1f, -1f);
        }
        var cameraPos = this.camera.pos();
        var lookAt = this.camera.lookAt();
        //modelViewStack.mulPoseMatrix(GTMatrixUtils.lookAt(cameraPos, lookAt));
        modelViewStack.last().pose().lookAt(cameraPos, lookAt, GTMath.UNIT_Y);
        modelViewStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        RenderSystem.applyModelViewMatrix();
    }

    protected void resetCamera() {
        // reset viewport
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.viewport(0, 0, minecraft.getWindow().getScreenWidth(), minecraft.getWindow().getScreenHeight());

        // reset projection matrix
        RenderSystem.restoreProjectionMatrix();

        // reset modelview matrix
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.disableBlend();
        RenderSystem.disableDepthTest();
        Lighting.setupForFlatItems();
    }

    @ApiStatus.OverrideOnly
    protected void onSetupCamera() {}

    @ApiStatus.OverrideOnly
    protected void onRendered() {}

    @ApiStatus.OverrideOnly
    protected void onSuccessfulRayTrace(GuiGraphics graphics, @NotNull BlockHitResult result) {}

    @ApiStatus.OverrideOnly
    protected void onRayTraceFailed() {}

    public boolean doRayTrace() {
        return false;
    }

    public int getClearColor() {
        return Color.withAlpha(0, 0.5f);
    }

    public boolean isIsometric() {
        return false;
    }

    public boolean isBEREnabled() {
        return true;
    }
}
