package com.gregtechceu.gtceu.utils.fakelevel;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.Icon;
import com.gregtechceu.gtceu.api.mui.widgets.SchemaWidget;
import lombok.Getter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.joml.Vector3f;

public class BaseSchemaRenderer implements IDrawable {

    private static final Framebuffer FBO = new Framebuffer(1080, 1080, true);

    @Getter
    private final ISchema schema;
    private final BlockGetter renderWorld;
    private final Framebuffer framebuffer;
    @Getter
    private final Camera camera = new Camera();
    private HitResult lastRayTrace = null;

    public BaseSchemaRenderer(ISchema schema, Framebuffer framebuffer) {
        this.schema = schema;
        this.framebuffer = framebuffer;
        this.renderWorld = new renderWorld.;
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


    protected void render(int x, int y, int width, int height, int mouseX, int mouseY) {
        onSetupCamera();
        int lastFbo = bindFBO();
        setupCamera(this.framebuffer.framebufferWidth, this.framebuffer.framebufferHeight);
        renderWorld();
        if (doRayTrace()) {
            HitResult result = null;
            if (Area.isInside(x, y, width, height, mouseX, mouseY)) {
                result = rayTrace(mouseX, mouseY, width, height);
            }
            if (result == null) {
                if (this.lastRayTrace != null) {
                    onRayTraceFailed();
                }
            } else {
                onSuccessfulRayTrace(result);
            }
            this.lastRayTrace = result;
        }
        onRendered();
        Platform.setupDrawTex();
        resetCamera();
        unbindFBO(lastFbo);

        // bind FBO as texture
        GlStateManager.enableTexture2D();
        GlStateManager.disableLighting();
        lastFbo = GL11.glGetInteger(GL11.GL_TEXTURE_2D);
        GlStateManager.bindTexture(this.framebuffer.framebufferTexture);
        GlStateManager.color(1, 1, 1, 1);

        // render rect with FBO texture
        Platform.startDrawing(Platform.DrawMode.QUADS, Platform.VertexFormat.POS_TEX, bufferBuilder -> {
            bufferBuilder.pos(x + width, y + height, 0).tex(1, 0).endVertex();
            bufferBuilder.pos(x + width, y, 0).tex(1, 1).endVertex();
            bufferBuilder.pos(x, y, 0).tex(0, 1).endVertex();
            bufferBuilder.pos(x, y + height, 0).tex(0, 0).endVertex();
        });
        GlStateManager.bindTexture(lastFbo);
    }

    protected HitResult rayTrace(int mouseX, int mouseY, int width, int height) {
        final float halfPI = (float) (Math.PI / 2);
        Vector3f cameraPos = camera.getPos();
        float yaw = camera.getYaw();
        float pitch = camera.getPitch();

        Vector3f mouseXShift = new Vector3f(1, 0, 0)
                .rotatePitch(pitch)
                .rotateYaw(-yaw + halfPI)
                .scale(mouseX - width / 2f)
                .scale(1 / 32f);
        Vector3f mouseYShift = new Vector3f(0, -1, 0)
                .rotatePitch(pitch)
                .rotateYaw(-yaw + halfPI)
                .scale(mouseY - height / 2f)
                .scale(1 / 32f);
        Vector3f mousePos = Vector3f.add(cameraPos, mouseXShift, mouseYShift, null);
        Vector3f focus = camera.getLookAt();
        float perspectiveCompensation = isIsometric() ? 1 : cameraPos.distanceTo(focus) / 3 * width / 100;
        Vector3f underMousePos = focus.add(mouseXShift.scale(perspectiveCompensation), null).add(mouseYShift.scale(perspectiveCompensation));
        Vector3f look = Vector3f.sub(underMousePos, mousePos, null).scale(10);
        Vector3f.add(mousePos, look, underMousePos);
        return schema.getWorld().rayTraceBlocks(mousePos.toVec3d(), underMousePos.toVec3d(), true);
    }

    private void renderWorld() {
        Minecraft mc = Minecraft.getMinecraft();
        Platform.setupDrawTex();
        GlStateManager.enableCull();
        GlStateManager.enableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        mc.entityRenderer.disableLightmap();
        mc.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        BlockRenderLayer oldRenderLayer = MinecraftForgeClient.getRenderLayer();
        GlStateManager.disableLighting();
        Platform.setupDrawGradient(); // needed for ambient occlusion

        try { // render block in each layer
            for (BlockRenderLayer layer : BlockRenderLayer.values()) {
                ForgeHooksClient.setRenderLayer(layer);
                int pass = layer == BlockRenderLayer.TRANSLUCENT ? 1 : 0;
                setDefaultPassRenderState(pass);
                BufferBuilder buffer = Tessellator.getInstance().getBuffer();
                buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
                BlockRendererDispatcher blockrendererdispatcher = mc.getBlockRendererDispatcher();
                this.schema.forEach(pair -> {
                    BlockPos pos = pair.getKey();
                    IBlockState state = pair.getValue().getBlockState();
                    if (!state.getBlock().isAir(state, this.renderWorld, pos) && state.getBlock().canRenderInLayer(state, layer)) {
                        blockrendererdispatcher.renderBlock(state, pos, this.renderWorld, buffer);
                    }
                });
                Tessellator.getInstance().draw();
                Tessellator.getInstance().getBuffer().setTranslation(0, 0, 0);
            }
        } finally {
            ForgeHooksClient.setRenderLayer(oldRenderLayer);
        }

        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableLighting();

        // render TESR
        if (isTesrEnabled()) {
            for (int pass = 0; pass < 2; pass++) {
                ForgeHooksClient.setRenderPass(pass);
                int finalPass = pass;
                GlStateManager.color(1, 1, 1, 1);
                setDefaultPassRenderState(pass);
                this.schema.forEach(pair -> {
                    BlockPos pos = pair.getKey();
                    TileEntity tile = pair.getValue().getTileEntity();
                    if (tile != null && tile.shouldRenderInPass(finalPass)) {
                        TileEntityRendererDispatcher.instance.render(tile, pos.getX(), pos.getY(), pos.getZ(), 0);
                    }
                });
            }
        }
        Platform.endDrawGradient();
        ForgeHooksClient.setRenderPass(-1);
        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    private static void setDefaultPassRenderState(int pass) {
        GlStateManager.color(1, 1, 1, 1);
        if (pass == 0) { // SOLID
            GlStateManager.enableDepth();
            GlStateManager.disableBlend();
            GlStateManager.depthMask(true);
        } else { // TRANSLUCENT
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GlStateManager.depthMask(false);
        }
    }

    protected final void setupCamera(int width, int height) {
        //GlStateManager.pushAttrib();

        Minecraft.getMinecraft().entityRenderer.disableLightmap();
        GlStateManager.disableLighting();
        GlStateManager.enableDepth();
        GlStateManager.enableBlend();

        // setup viewport and clear GL buffers
        GlStateManager.viewport(0, 0, width, height);
        Color.setGlColor(getClearColor());
        GlStateManager.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        // setup projection matrix to perspective
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        float near = isIsometric() ? 1f : 0.1f;
        float far = 10000.0f;
        float fovY = 60.0f; // Field of view in the Y direction
        float aspect = (float) width / height; // width and height are the dimensions of your window
        float top = near * (float) Math.tan(Math.toRadians(fovY) / 2.0);
        float bottom = -top;
        float left = aspect * bottom;
        float right = aspect * top;
        if (isIsometric()) {
            GL11.glOrtho(left, right, bottom, top, near, far);
        } else {
            GL11.glFrustum(left, right, bottom, top, near, far);
        }

        // setup modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();
        if (isIsometric()) {
            GlStateManager.scale(0.1, 0.1, 0.1);
        }
        var c = this.camera.getPos();
        var lookAt = this.camera.getLookAt();
        GLU.gluLookAt(c.x, c.y, c.z, lookAt.x, lookAt.y, lookAt.z, 0, 1, 0);
    }

    protected final void resetCamera() {
        // reset viewport
        Minecraft minecraft = Minecraft.getMinecraft();
        GlStateManager.viewport(0, 0, minecraft.displayWidth, minecraft.displayHeight);
        // reset projection matrix
        GlStateManager.matrixMode(GL11.GL_PROJECTION);
        GlStateManager.popMatrix();
        // reset modelview matrix
        GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        GlStateManager.popMatrix();
        GlStateManager.disableBlend();
        GlStateManager.disableDepth();
        // reset attributes
        // GlStateManager.popAttrib();
    }

    private int bindFBO() {
        int lastID = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        this.framebuffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
        this.framebuffer.framebufferClear();
        this.framebuffer.bindFramebuffer(true);
        GlStateManager.pushMatrix();
        return lastID;
    }

    private void unbindFBO(int lastID) {
        GlStateManager.popMatrix();
        this.framebuffer.unbindFramebufferTexture();
        OpenGlHelper.glBindFramebuffer(OpenGlHelper.GL_FRAMEBUFFER, lastID);
    }

    @ApiStatus.OverrideOnly
    protected void onSetupCamera() {}

    @ApiStatus.OverrideOnly
    protected void onRendered() {}

    @ApiStatus.OverrideOnly
    protected void onSuccessfulRayTrace(@NotNull RayTraceResult result) {}

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

    public boolean isTesrEnabled() {
        return true;
    }
}


