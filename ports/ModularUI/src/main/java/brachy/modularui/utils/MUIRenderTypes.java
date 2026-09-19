package brachy.modularui.utils;

import brachy.modularui.drawable.GuiShapeBuilder;
import brachy.modularui.drawable.GuiShapeRenderState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** GUI geometry factories. Submit each builder to the current graphics extractor after filling it. */
@OnlyIn(Dist.CLIENT)
public final class MUIRenderTypes {
    private MUIRenderTypes() {}

    public static RenderPipeline guiTexture() {
        return RenderPipelines.GUI_TEXTURED;
    }

    public static GuiShapeBuilder guiTriangleStrip() {
        return new GuiShapeBuilder(GuiShapeRenderState.Topology.TRIANGLE_STRIP);
    }

    public static GuiShapeBuilder guiTriangleFan() {
        return new GuiShapeBuilder(GuiShapeRenderState.Topology.TRIANGLE_FAN);
    }

    public static GuiShapeBuilder guiOverlayTriangleFan() {
        return guiTriangleFan();
    }
}
