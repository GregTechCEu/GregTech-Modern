package com.gregtechceu.gtceu.common.mui.widgets;

import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.MUIRenderTypes;
import brachy.modularui.value.sync.DoubleSyncValue;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.joml.Matrix4f;

@Accessors(chain = true)
public class SteamDialWidget implements IDrawable {

    private final DoubleSyncValue progress;
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
        Matrix4f pose = graphics.pose().last().pose();

        float halfH = height / 2.0f;

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

        if (texture == null) {
            VertexConsumer bufferBuilder = graphics.bufferSource().getBuffer(MUIRenderTypes.guiTriangleStrip());

            bufferBuilder.addVertex(pose, x + width * cosAngle, y + width * sinAngle, 0.0f).setColor(color);
            bufferBuilder.addVertex(pose, x + halfH * sinAngle, y - halfH * cosAngle, 0.0f).setColor(color);
            bufferBuilder.addVertex(pose, x - halfH * sinAngle, y + halfH * cosAngle, 0.0f).setColor(color);
            bufferBuilder.addVertex(pose, x - halfH * cosAngle, y - halfH * sinAngle, 0.0f).setColor(color);
        } else {
            VertexConsumer bufferBuilder = graphics.bufferSource()
                    .getBuffer(GTRenderTypes.guiTriangleStrip(texture.location));

            bufferBuilder.addVertex(pose, x + width * cosAngle, y + width * sinAngle, 0.0f).setUv(0.0f, 0.0f)
                    .setColor(color);
            bufferBuilder.addVertex(pose, x + halfH * sinAngle, y - halfH * cosAngle, 0.0f).setUv(1.0f, 0.0f)
                    .setColor(color);
            bufferBuilder.addVertex(pose, x - halfH * sinAngle, y + halfH * cosAngle, 0.0f).setUv(0.0f, 1.0f)
                    .setColor(color);
            bufferBuilder.addVertex(pose, x - halfH * cosAngle, y - halfH * sinAngle, 0.0f).setUv(1.0f, 1.0f)
                    .setColor(color);
        }
    }
}
