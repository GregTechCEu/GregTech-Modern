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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
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
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexSorting;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
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
    private final int[] viewport = { 0, 0, 0, 0 };
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

        this.renderTarget.setClearColor(0, 0, 0, 0);
        this.renderTarget.clear(Minecraft.ON_OSX);
        this.renderTarget.bindWrite(true);

        this.viewport[0] = 0;
        this.viewport[1] = 0;
        this.viewport[2] = this.renderTarget.viewWidth;
        this.viewport[3] = this.renderTarget.viewHeight;

        context.getGraphics().pose().pushPose();
        setupCamera(this.renderTarget.viewWidth, this.renderTarget.viewHeight);
        renderWorld();
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
                onSuccessfulRayTrace(RenderSystem.getModelViewStack(), result);
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
        Vector3f cameraPos = camera.pos();
        float yaw = camera.yaw();
        float pitch = camera.pitch();

        Vector3f mouseXShift = new Vector3f(1, 0, 0)
                .rotateX(pitch)
                .rotateY(-yaw + GTMath.PI_HALF)
                .mul(mouseX - width / 2f)
                .mul(1 / 32f);
        Vector3f mouseYShift = new Vector3f(0, -1, 0)
                .rotateX(pitch)
                .rotateY(-yaw + GTMath.PI_HALF)
                .mul(mouseY - height / 2f)
                .mul(1 / 32f);
        Vector3f levelMouse = cameraPos.add(mouseXShift, new Vector3f()).add(mouseYShift);
        Vector3f focus = camera.lookAt();
        float perspectiveCompensation = isIsometric() ? 1 : cameraPos.distance(focus) / 3 * width / 100;
        Vector3f target = focus.add(mouseXShift.mul(perspectiveCompensation), new Vector3f())
                .add(mouseYShift.mul(perspectiveCompensation));
        Vector3f look = target.sub(levelMouse, new Vector3f()).mul(10);
        levelMouse.add(look, target);
        // TODO proper raytracing by matrix inversion
        /*
         * mouseX = (int) ((float) mouseX / width * this.viewport[2]);
         * mouseY = (int) ((float) mouseY / height * this.viewport[3]);
         * Vector3f levelMouse = screenPosToLevelPos(mouseX, mouseY);
         * Vector3f target = this.camera.getLookVec().mul(20).add(levelMouse);
         * GTCEu.LOGGER.info("Raytracing at {}, {}. Level pos: {}, Target: {}",
         * mouseX, mouseY, new BlockPos((int) levelMouse.x, (int) levelMouse.y, (int) levelMouse.z),
         * new BlockPos((int) target.x, (int) target.y, (int) target.z));
         */
        ClipContext context = new ClipContext(new Vec3(levelMouse), new Vec3(target), ClipContext.Block.VISUAL,
                ClipContext.Fluid.ANY,
                null);
        return schema.getLevel().clip(context);
    }

    private void renderWorld() {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        RandomSource random = RandomSource.createNewThreadLocalInstance();

        Minecraft mc = Minecraft.getInstance();
        RenderSystem.enableCull();
        mc.gameRenderer.lightTexture().turnOnLightLayer();
        RenderSystem.activeTexture(org.lwjgl.opengl.GL13.GL_TEXTURE0);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);

        var bufferSource = mc.renderBuffers().bufferSource();

        List<BlockEntity> ber = null;
        // render block in each layer
        List<RenderType> chunkBufferLayers = RenderType.chunkBufferLayers();
        for (int i = 0; i < chunkBufferLayers.size(); i++) {
            RenderType layer = chunkBufferLayers.get(i);
            if (i == 0 && isBEREnabled()) {
                ber = renderBlocksInLayer(mc, bufferSource, poseStack, layer, random, true);
            } else {
                renderBlocksInLayer(mc, bufferSource, poseStack, layer, random, false);
            }

        }

        // render TESR
        if (ber != null && !ber.isEmpty()) {
            renderTesr(mc, bufferSource, poseStack, ber);
        }

        bufferSource.endBatch();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    public static void setDefaultRenderLayerState(RenderType layer) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        if (layer == RenderType.translucent()) { // TRANSLUCENT
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.depthMask(false);
        } else { // SOLID
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.depthMask(true);
        }
    }

    private List<BlockEntity> renderBlocksInLayer(Minecraft mc, MultiBufferSource.BufferSource bufferSource,
                                                  PoseStack pose,
                                                  RenderType type, RandomSource random, boolean collectBer) {
        type.setupRenderState();
        setDefaultRenderLayerState(type);
        List<BlockEntity> ber = collectBer ? new ArrayList<>() : null;

        VertexConsumer buffer = bufferSource.getBuffer(type);
        BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
        this.schema.forEach(pair -> {
            BlockPos pos = pair.getKey();
            BlockState state = pair.getValue().getBlockState();
            if (state.getRenderShape() == RenderShape.INVISIBLE) return;
            var be = pair.getValue().getBlockEntity();

            if (collectBer) {
                if (be != null && !be.isRemoved()) {
                    if (mc.getBlockEntityRenderDispatcher().getRenderer(be) != null) {
                        // only collect tiles to render which actually have a ber
                        ber.add(be);
                    }
                }
            }

            if (state.getRenderShape() != RenderShape.MODEL) return;

            ModelData modelData = ModelData.EMPTY;
            // normally get cached model data from ModelDataManager from level
            // but our dummy level doesn't have that
            if (be != null) {
                modelData = be.getModelData();
            }
            var blockModel = blockRenderer.getBlockModel(state);
            if (!blockModel.getRenderTypes(state, random, modelData).contains(type)) return;
            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            blockRenderer.getModelRenderer()
                    .tesselateBlock(this.renderLevel, blockModel, state, pos, pose, buffer, true, random,
                            state.getSeed(pos),
                            OverlayTexture.NO_OVERLAY, modelData, type);
            pose.popPose();

        });
        bufferSource.endBatch(type);
        type.clearRenderState();
        return ber;
    }

    private static void renderTesr(Minecraft mc, MultiBufferSource.BufferSource bufferSource, PoseStack poseStack,
                                   List<BlockEntity> tileEntities) {
        RenderSystem.setShaderColor(1, 1, 1, 1);

        for (Iterator<BlockEntity> iterator = tileEntities.iterator(); iterator.hasNext();) {
            BlockEntity tile = iterator.next();
            if (tile == null || tile.isRemoved()) continue;

            mc.getBlockEntityRenderDispatcher().render(tile, mc.getPartialTick(), poseStack, bufferSource);
        }
    }

    protected void setupCamera(int width, int height) {
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        // setup viewport and clear GL buffers
        RenderSystem.viewport(0, 0, width, height);
        RenderSystem.depthMask(true);
        int clearColor = getClearColor();
        RenderSystem.clearColor(Color.getRedF(clearColor), Color.getGreenF(clearColor), Color.getBlueF(clearColor),
                Color.getAlphaF(clearColor));
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.backupProjectionMatrix();

        float near = isIsometric() ? 1f : 0.1f;
        float far = 10000.0f;
        float fovY = (float) Math.toRadians(60.0f); // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        float top = -near * (float) Math.tan(fovY / 2.0);
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
        RenderSystem.applyModelViewMatrix();
        if (isIsometric()) {
            modelViewStack.scale(0.1f, 0.1f, 0.1f);
        } else {
            // modelViewStack.scale(-1f, -1f, -1f);
        }
        var cameraPos = this.camera.pos();
        var lookAt = this.camera.lookAt();
        modelViewStack.last().pose().lookAt(cameraPos, lookAt, GTMath.UNIT_Y);
    }

    protected void resetCamera() {
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        // reset viewport
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.viewport(0, 0, minecraft.getWindow().getWidth(), minecraft.getWindow().getHeight());

        // reset projection matrix
        RenderSystem.restoreProjectionMatrix();

        // reset modelview matrix
        RenderSystem.getModelViewStack().popPose();
        RenderSystem.applyModelViewMatrix();

        RenderSystem.depthMask(false);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
    }

    protected Vector3f screenPosToLevelPos(int wx, int wy) {
        // TODO in theory this should work, but the resulting raytracer always hits the block in the center of the
        // framebuffer texture
        Matrix4f levelTransform = new Matrix4f(RenderSystem.getProjectionMatrix())
                .mul(RenderSystem.getModelViewStack().last().pose());
        return levelTransform.unproject(wx, wy, -1f, this.viewport, new Vector3f());
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
