package brachy.modularui.client;

import brachy.modularui.drawable.GuiEntityPreviewState;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Quaternionf;

/** Uses Minecraft's entity submission and GUI picture-in-picture lifecycle. */
public final class GuiEntityPreviewRenderer extends PictureInPictureRenderer<GuiEntityPreviewState> {
    @Override
    public Class<GuiEntityPreviewState> getRenderStateClass() {
        return GuiEntityPreviewState.class;
    }

    @Override
    protected void renderToTexture(GuiEntityPreviewState state, PoseStack pose, SubmitNodeCollector collector) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.lighting().setupFor(Lighting.Entry.ENTITY_IN_UI);
        pose.translate(state.translation().x(), state.translation().y(), state.translation().z());
        pose.mulPose(state.rotation());
        CameraRenderState camera = new CameraRenderState();
        if (state.cameraAngle() != null) {
            camera.orientation = state.cameraAngle().conjugate(new Quaternionf()).rotateY((float) Math.PI);
        }
        minecraft.getEntityRenderDispatcher().submit(state.entity(), camera, 0, 0, 0, pose, collector);
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2f;
    }

    @Override
    protected String getTextureLabel() {
        return "modularui_entity_preview";
    }
}
