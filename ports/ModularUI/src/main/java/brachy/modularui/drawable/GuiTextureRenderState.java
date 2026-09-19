package brachy.modularui.drawable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;

/** A float-precision textured quad with its transform, tint, texture and clip captured for deferred drawing. */
public record GuiTextureRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2fc pose,
                                     float x0, float y0, float x1, float y1,
                                     float u0, float v0, float u1, float v1, int color,
                                     @Nullable ScreenRectangle scissorArea,
                                     @Nullable ScreenRectangle bounds) implements GuiElementRenderState {
    public GuiTextureRenderState {
        pose = new Matrix3x2f(pose);
    }

    public static void submit(GuiGraphicsExtractor graphics, Identifier location,
                              float x0, float y0, float x1, float y1,
                              float u0, float v0, float u1, float v1, boolean blend) {
        if (x0 == x1 || y0 == y1) return;
        int left = (int) Math.floor(Math.min(x0, x1)), top = (int) Math.floor(Math.min(y0, y1));
        int right = (int) Math.ceil(Math.max(x0, x1)), bottom = (int) Math.ceil(Math.max(y0, y1));
        ScreenRectangle bounds = new ScreenRectangle(left, top, right - left, bottom - top)
                .transformMaxBounds(graphics.pose());
        ScreenRectangle scissor = graphics.peekScissorStack();
        if (scissor != null) bounds = bounds.intersection(scissor);
        if (bounds == null) return;
        var texture = Minecraft.getInstance().getTextureManager().getTexture(location);
        graphics.submitGuiElementRenderState(new GuiTextureRenderState(
                blend ? RenderPipelines.GUI_TEXTURED : RenderPipelines.GUI_OPAQUE_TEXTURED_BACKGROUND,
                TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()), graphics.pose(),
                x0, y0, x1, y1, u0, v0, u1, v1, GuiTint.get(), scissor, bounds));
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        consumer.addVertexWith2DPose(pose, x0, y1).setUv(u0, v1).setColor(color);
        consumer.addVertexWith2DPose(pose, x1, y1).setUv(u1, v1).setColor(color);
        consumer.addVertexWith2DPose(pose, x1, y0).setUv(u1, v0).setColor(color);
        consumer.addVertexWith2DPose(pose, x0, y0).setUv(u0, v0).setColor(color);
    }
}
