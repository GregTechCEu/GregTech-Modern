package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.widget.sizer.Area;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.GTMatrixUtils;
import com.gregtechceu.gtceu.utils.fakelevel.Camera;
import com.gregtechceu.gtceu.utils.fakelevel.ISchema;
import com.gregtechceu.gtceu.utils.fakelevel.RenderLevel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.DoubleSupplier;

@Accessors(fluent = true, chain = true)
public class SchemaRenderer implements IDrawable {

    private static final RenderTarget FBO = new TextureTarget(1080, 1080, true, Minecraft.ON_OSX);

    private final ISchema schema;
    private final LevelReader renderLevel;
    private final RenderTarget renderTarget;
    private final Camera camera = new Camera(new Vector3f(), new Vector3f());
    @Setter
    private boolean cameraSetup = false;
    @Setter
    private DoubleSupplier scale;
    @Setter
    private BooleanSupplier disableBER;
    @Setter
    private Consumer<IRayTracer> onRayTrace;
    @Setter
    private Runnable afterRender;
    @Setter
    private BiConsumer<Camera, ISchema> cameraFunc;
    private int clearColor = 0;
    @Setter
    private boolean isometric = false;

    public SchemaRenderer(ISchema schema, RenderTarget renderTarget) {
        this.schema = schema;
        this.renderTarget = renderTarget;
        this.renderLevel = new RenderLevel(schema);
    }

    public SchemaRenderer(ISchema schema) {
        this(schema, FBO);
    }

    @Tolerate
    public SchemaRenderer scale(double scale) {
        return scale(() -> scale);
    }

    @Tolerate
    public SchemaRenderer disableBER(boolean disable) {
        return disableBER(() -> disable);
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        render(context, x, y, width, height, context.getMouseX(), context.getMouseY());
    }

    public void render(GuiContext context, int x, int y, int width, int height, int mouseX, int mouseY) {
        if (this.cameraFunc != null) {
            this.cameraFunc.accept(this.camera, this.schema);
        }
        if (Objects.nonNull(scale)) {
            Vector3f cameraPos = camera.pos();
            Vector3f looking = camera.lookAt();
            cameraPos.sub(looking);
            if (cameraPos.length() != 0.0f) cameraPos.normalize();
            cameraPos.mul((float) scale.getAsDouble());
            cameraPos.add(looking);
        }
        this.renderTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.renderTarget.clear(Minecraft.ON_OSX);
        this.renderTarget.bindWrite(true);

        context.getGraphics().pose().pushPose();
        setupCamera(this.renderTarget.viewWidth, this.renderTarget.viewHeight);
        renderWorld(context);
        if (this.onRayTrace != null && Area.isInside(x, y, width, height, mouseX, mouseY)) {
            this.onRayTrace.accept(new IRayTracer() {

                @Override
                public HitResult rayTrace(int screenX, int screenY) {
                    return SchemaRenderer.this.rayTrace(GTMatrixUtils.projectScreenToWorld(screenX, screenY));
                }

                @Override
                public HitResult rayTraceMousePos() {
                    return rayTrace(mouseX, mouseY);
                }
            });
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
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        bufferbuilder.vertex(x + width, y + height, 0).uv(1, 0).endVertex();
        bufferbuilder.vertex(x + width, y, 0).uv(1, 1).endVertex();
        bufferbuilder.vertex(x, y, 0).uv(0, 1).endVertex();
        bufferbuilder.vertex(x, y + height, 0).uv(0, 0).endVertex();
        tesselator.end();
    }

    private void renderWorld(GuiContext context) {
        PoseStack poseStack = context.getGraphics().pose();
        MultiBufferSource.BufferSource bufferSource = context.getGraphics().bufferSource();
        RandomSource random = RandomSource.create();

        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        RenderSystem.enableCull();
        Lighting.setupForFlatItems();
        mc.gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);

        for (final RenderType type : RenderType.chunkBufferLayers()) {
            type.setupRenderState();
            BufferBuilder buffer = Tesselator.getInstance().getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
            BlockRenderDispatcher blockRenderer = mc.getBlockRenderer();
            this.schema.forEach(pair -> {
                BlockPos pos = pair.getKey();
                BlockState state = pair.getValue().getBlockState();
                if (state.getRenderShape() == RenderShape.INVISIBLE) return;

                var be = pair.getValue().getBlockEntity();
                ModelData modelData = ModelData.EMPTY;
                if (be != null) {
                    modelData = be.getModelData();
                }
                poseStack.pushPose();
                poseStack.translate(-cameraPos.x(), -cameraPos.y(), -cameraPos.z());
                blockRenderer.renderBatched(state, pos, this.renderLevel,
                        poseStack, buffer, true,
                        random, modelData, type);
                poseStack.popPose();
            });
            Tesselator.getInstance().end();
            type.clearRenderState();
        }

        Lighting.setupFor3DItems();

        // render BERs
        if (disableBER == null || !disableBER.getAsBoolean()) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            BlockEntityRenderDispatcher blockEntityRenderer = mc.getBlockEntityRenderDispatcher();
            float partialTick = mc.getPartialTick();
            this.schema.forEach(pair -> {
                BlockPos pos = pair.getKey();
                if (pair.getValue().getBlockState().getRenderShape() == RenderShape.MODEL) return;
                BlockEntity be = pair.getValue().getBlockEntity();
                if (be == null) return;
                poseStack.pushPose();
                poseStack.translate(pos.getX() - cameraPos.x(), pos.getY() - cameraPos.y(), pos.getZ() - cameraPos.z());
                blockEntityRenderer.render(be, partialTick, poseStack, bufferSource);
                poseStack.popPose();
            });
        }
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        if (this.afterRender != null) {
            this.afterRender.run();
        }
    }

    protected void setupCamera(int width, int height) {
        Minecraft.getInstance().gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        // setup viewport and clear GL buffers
        RenderSystem.viewport(0, 0, width, height);
        Color.setGlColor(clearColor);
        RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.backupProjectionMatrix();

        float near = this.isometric ? 1f : 0.1f;
        float far = 10000.0f;
        float fovY = 60.0f; // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        Matrix4f projection = new Matrix4f();
        if (this.isometric) {
            float top = near * (float) Math.tan(Math.toRadians(fovY) / 2.0);
            float bottom = -top;
            float left = aspect * bottom;
            float right = aspect * top;
            projection.setOrtho(left, right, bottom, top, near, far);
            RenderSystem.setProjectionMatrix(projection, VertexSorting.ORTHOGRAPHIC_Z);
        } else {
            projection.setPerspective(fovY, aspect, near, far);
            RenderSystem.setProjectionMatrix(projection, VertexSorting.byDistance(camera.lookAt()));
        }

        // setup modelview matrix
        PoseStack modelViewStack = RenderSystem.getModelViewStack();
        modelViewStack.pushPose();
        modelViewStack.setIdentity();
        if (this.isometric) {
            modelViewStack.scale(0.1f, 0.1f, 0.1f);
        }
        var cameraPos = this.camera.pos();
        var lookAt = this.camera.lookAt();
        modelViewStack.mulPoseMatrix(GTMatrixUtils.lookAt(cameraPos, lookAt));
        RenderSystem.applyModelViewMatrix();
        this.cameraSetup = true;
    }

    protected void resetCamera() {
        this.cameraSetup = false;
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
    }

    private HitResult rayTrace(Vector3f hitPos) {
        Vec3 startPos = new Vec3(this.camera.pos().x, this.camera.pos().y, this.camera.pos().z);
        hitPos.mul(2); // Double view range to ensure pos can be seen.
        Vec3 endPos = new Vec3((hitPos.x - startPos.x), (hitPos.y - startPos.y), (hitPos.z - startPos.z));

        ClipContext context = new ClipContext(startPos, endPos,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null);
        return this.schema.getLevel().clip(context);
    }

    public boolean isCameraSetup() {
        return cameraSetup;
    }

    public interface IRayTracer {

        HitResult rayTrace(int screenX, int screenY);

        HitResult rayTraceMousePos();
    }

    public interface ICamera {

        void setupCamera(Vector3f cameraPos, Vector3f lookAt);
    }
}
