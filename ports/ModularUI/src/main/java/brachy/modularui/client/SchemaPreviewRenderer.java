package brachy.modularui.client;

import brachy.modularui.drawable.schema.DummyLightTexture;
import brachy.modularui.drawable.schema.SchemaCameraTransform;
import brachy.modularui.drawable.schema.SchemaRenderState;
import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.platform.CompareOp;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Draws extracted structure geometry through the game's feature and GUI render passes. */
public final class SchemaPreviewRenderer extends PictureInPictureRenderer<SchemaRenderState> {
    private static final RenderType HIGHLIGHT = RenderType.create("modularui_structure_highlight",
            RenderSetup.builder(net.minecraft.client.renderer.RenderPipelines.DEBUG_QUADS.toBuilder()
                    .withLocation(Identifier.fromNamespaceAndPath("modularui", "structure_highlight"))
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false)).build())
                    .sortOnUpload().createRenderSetup());
    private static final ThreadLocal<GpuTextureView> ACTIVE_LIGHTMAP = new ThreadLocal<>();
    private final DummyLightTexture lightmap = new DummyLightTexture();
    private final ProjectionMatrixBuffer projection = new ProjectionMatrixBuffer("ModularUI structure");
    private FogRenderer fog;

    public static GpuTextureView activeLightmap() { return ACTIVE_LIGHTMAP.get(); }

    @Override public Class<SchemaRenderState> getRenderStateClass() { return SchemaRenderState.class; }
    @Override protected String getTextureLabel() { return "modularui_structure_preview"; }

    @Override
    public void prepare(SchemaRenderState state, GuiRenderState gui, FeatureRenderDispatcher features, int scale) {
        var previousProjection = RenderSystem.getProjectionMatrixBuffer();
        var previousType = RenderSystem.getProjectionType();
        var previousFog = RenderSystem.getShaderFog();
        var previousLights = RenderSystem.getShaderLights();
        var previousColor = RenderSystem.outputColorTextureOverride;
        var previousDepth = RenderSystem.outputDepthTextureOverride;
        var previousLightmap = ACTIVE_LIGHTMAP.get();
        try {
            lightmap.render(state.lightmap());
            ACTIVE_LIGHTMAP.set(lightmap.getTextureView());
            if (fog == null) fog = new FogRenderer();
            RenderSystem.setShaderFog(fog.getBuffer(FogRenderer.FogMode.NONE));
            super.prepare(state, gui, features, scale);
        } finally {
            if (previousLightmap == null) ACTIVE_LIGHTMAP.remove();
            else ACTIVE_LIGHTMAP.set(previousLightmap);
            RenderSystem.setProjectionMatrix(previousProjection, previousType);
            RenderSystem.setShaderFog(previousFog);
            RenderSystem.setShaderLights(previousLights);
            RenderSystem.outputColorTextureOverride = previousColor;
            RenderSystem.outputDepthTextureOverride = previousDepth;
        }
    }

    @Override
    protected void renderToTexture(SchemaRenderState state, PoseStack pose, SubmitNodeCollector collector) {
        var matrix = SchemaCameraTransform.projection(state.x1() - state.x0(), state.y1() - state.y0(),
                state.orthographic(), state.orthoHeight(), true, RenderSystem.getDevice().getDeviceInfo().isZZeroToOne());
        RenderSystem.setProjectionMatrix(projection.getBuffer(matrix),
                state.orthographic() ? ProjectionType.ORTHOGRAPHIC : ProjectionType.PERSPECTIVE);
        // Keep vertices camera-relative so the feature renderer sorts transparency correctly.
        RenderSystem.getModelViewStack().set(new Matrix4f(state.view()).setTranslation(0, 0, 0));
        pose.setIdentity();
        pose.translate(-state.camera().x(), -state.camera().y(), -state.camera().z());
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        for (var layer : state.layers()) {
            collector.submitCustomGeometry(pose, layer.type(), (transform, consumer) ->
                    layer.vertices().forEach(vertex -> vertex.write(transform, consumer)));
        }
        CameraRenderState camera = new CameraRenderState();
        camera.pos = new Vec3(state.camera());
        camera.initialized = true;
        state.view().getUnnormalizedRotation(camera.orientation).conjugate();
        for (var entity : state.blockEntities()) {
            pose.pushPose();
            pose.translate(entity.blockPos.getX(), entity.blockPos.getY(), entity.blockPos.getZ());
            minecraft.getBlockEntityRenderDispatcher().submit(entity, pose, collector, camera);
            pose.popPose();
        }
        if (!state.highlight().isEmpty()) {
            collector.order(1).submitCustomGeometry(pose, HIGHLIGHT, (transform, consumer) ->
                    state.highlight().forEach(vertex -> vertex.write(transform, consumer)));
        }
    }

    @Override public void close() {
        super.close();
        projection.close();
        lightmap.close();
        if (fog != null) fog.close();
    }
}
