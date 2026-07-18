package com.gregtechceu.gtceu.common.mui.widgets;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.value.sync.DoubleSyncValue;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Matrix4f;

@Accessors(chain = true)
public class SteamDialWidget implements IDrawable {

    private DoubleSyncValue progress;
    @Setter
    private float minAngle;
    @Setter
    private float maxAngle;
    @Setter
    private int color;
    private float lastAngle = Float.NaN;
    @Setter
    private UITexture texture;

    public SteamDialWidget(DoubleSyncValue progress) {
        this.progress = progress;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiGraphics graphics = context.getGraphics();
        // RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f pose = graphics.pose().last().pose();
        if (texture == null) {
            VertexConsumer bufferBuilder = graphics.bufferSource().getBuffer(GTRenderTypes.guiTriangleStrip());

            final float progressPercent = Mth.clamp(progress.getFloatValue(), 0.0f, 1.0f);
            final float angle = Mth.lerp(progressPercent, this.minAngle, this.maxAngle);

            if (Float.isNaN(lastAngle)) {
                lastAngle = angle;
            } else {
                lastAngle = (lastAngle + angle) / 2.0f;
            }
            final float lastAngleF = lastAngle;

            final float sinAngle = Mth.sin(-lastAngleF);
            final float cosAngle = Mth.cos(-lastAngleF);

            height /= 2.f;
            int a = Color.getAlpha(color), r = Color.getRed(color), g = Color.getGreen(color), b = Color.getBlue(color);

            bufferBuilder.vertex(pose, x + width * cosAngle, y + width * sinAngle, 0.0f).color(r, g, b, a).endVertex();
            bufferBuilder.vertex(pose, x + height * sinAngle, y - height * cosAngle, 0.0f).color(r, g, b, a)
                    .endVertex();
            bufferBuilder.vertex(pose, x - height * sinAngle, y + height * cosAngle, 0.0f).color(r, g, b, a)
                    .endVertex();
            bufferBuilder.vertex(pose, x - height * cosAngle, y - height * sinAngle, 0.0f).color(r, g, b, a)
                    .endVertex();
        } else {
            VertexConsumer bufferBuilder = graphics.bufferSource()
                    .getBuffer(GTRenderTypes.guiTriangleStrip(texture.location));

            final float progressPercent = Mth.clamp(progress.getFloatValue(), 0.0f, 1.0f);
            final float angle = Mth.lerp(progressPercent, this.minAngle, this.maxAngle);

            if (Float.isNaN(lastAngle)) {
                lastAngle = angle;
            } else {
                lastAngle = (lastAngle + angle) / 2.0f;
            }
            final float lastAngleF = lastAngle;

            final float sinAngle = Mth.sin(-lastAngleF);
            final float cosAngle = Mth.cos(-lastAngleF);

            height /= 2.f;
            int a = Color.getAlpha(color), r = Color.getRed(color), g = Color.getGreen(color), b = Color.getBlue(color);

            bufferBuilder.vertex(pose, x + width * cosAngle, y + width * sinAngle, 0.0f).color(r, g, b, a)
                    .uv(0.0f, 0.0f).endVertex();
            bufferBuilder.vertex(pose, x + height * sinAngle, y - height * cosAngle, 0.0f).color(r, g, b, a)
                    .uv(1.0f, 0.0f).endVertex();
            bufferBuilder.vertex(pose, x - height * sinAngle, y + height * cosAngle, 0.0f).color(r, g, b, a)
                    .uv(0.0f, 1.0f).endVertex();
            bufferBuilder.vertex(pose, x - height * cosAngle, y - height * sinAngle, 0.0f).color(r, g, b, a)
                    .uv(1.0f, 1.0f).endVertex();
        }
        // RenderSystem.disableBlend();
    }
}
