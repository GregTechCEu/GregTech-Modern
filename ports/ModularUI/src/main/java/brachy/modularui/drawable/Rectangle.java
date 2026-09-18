package brachy.modularui.drawable;

import brachy.modularui.utils.MUIRenderTypes;
import brachy.modularui.ModularUI;
import brachy.modularui.animation.IAnimatable;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Interpolations;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.joml.Matrix4f;

@ToString
@Accessors(fluent = true, chain = true)
public class Rectangle implements IDrawable, IAnimatable<Rectangle> {

    public static final MutableObjectCodec<Rectangle> CODEC = MutableObjectCodec.drawableBuilder(Rectangle::new)
            .addOpt("colorTopLeft", Rectangle::colorTL, Rectangle::colorTL, Color.CODEC, Color.WHITE.main)
            .alias("colorTL", "colorLeft", "colorTop", "color")
            .addOpt("colorTopRight", Rectangle::colorTR, Rectangle::colorTR, Color.CODEC, Color.WHITE.main)
            .alias("colorTR", "colorRight", "colorTop", "color")
            .addOpt("colorBottomLeft", Rectangle::colorBL, Rectangle::colorBL, Color.CODEC, Color.WHITE.main)
            .alias("colorBL", "colorLeft", "colorBottom", "color")
            .addOpt("colorBottomRight", Rectangle::colorBR, Rectangle::colorBR, Color.CODEC, Color.WHITE.main)
            .alias("colorBR", "colorRight", "colorBottom", "color")
            .addOpt("cornerRadius", Rectangle::cornerRadius, Rectangle::cornerRadius, Codec.INT, 0)
            .addOpt("cornerSegments", Rectangle::cornerSegments, Rectangle::cornerSegments, Codec.INT, 8)
            .addOpt("borderThickness", Rectangle::borderThickness, Rectangle::borderThickness, Codec.FLOAT, 0f)
            .addOpt("canApplyTheme", Rectangle::canApplyTheme, Rectangle::canApplyTheme, Codec.BOOL, false)
            .build();

    @Getter
    @Setter
    private int colorTL, colorTR, colorBL, colorBR;
    @Getter
    private int cornerRadius;
    @Getter
    @Setter
    private int cornerSegments;
    @Getter
    @Setter
    private float borderThickness;
    @Getter
    @Setter
    private boolean canApplyTheme = false;

    public Rectangle() {
        color(0xFFFFFFFF);
        this.cornerRadius = 0;
        this.cornerSegments = 6;
    }

    public int getColor() {
        return this.colorTL;
    }

    public Rectangle cornerRadius(int cornerRadius) {
        this.cornerRadius = Math.max(0, cornerRadius);
        if (this.borderThickness > 0 && cornerRadius > 0) {
            ModularUI.LOGGER.error("Hollow rectangles currently can't have a corner radius.");
        }
        return this;
    }

    public Rectangle color(int colorTL, int colorTR, int colorBL, int colorBR) {
        this.colorTL = colorTL;
        this.colorTR = colorTR;
        this.colorBL = colorBL;
        this.colorBR = colorBR;
        return this;
    }

    public Rectangle verticalGradient(int colorTop, int colorBottom) {
        return color(colorTop, colorTop, colorBottom, colorBottom);
    }

    public Rectangle horizontalGradient(int colorLeft, int colorRight) {
        return color(colorLeft, colorRight, colorLeft, colorRight);
    }

    public Rectangle color(int color) {
        return color(color, color, color, color);
    }

    public Rectangle solid() {
        this.borderThickness = 0;
        return this;
    }

    public Rectangle hollow(float borderThickness) {
        this.borderThickness = borderThickness;
        if (borderThickness > 0 && this.cornerRadius > 0) {
            ModularUI.LOGGER.error("Hollow rectangles currently can't have a corner radius.");
        }
        return this;
    }

    public Rectangle hollow() {
        return hollow(1);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        if (this.borderThickness <= 0) {
            if (this.cornerRadius <= 0) {
                GuiDraw.drawRect(context.getGraphics(), x0, y0, width, height,
                        this.colorTL, this.colorTR, this.colorBL, this.colorBR);
                return;
            }
            GuiDraw.drawRoundedRect(context.getGraphics(), x0, y0, width, height,
                    this.colorTL, this.colorTR, this.colorBL, this.colorBR,
                    this.cornerRadius, this.cornerSegments);
        } else {
            float d = this.borderThickness;
            float x1 = x0 + width, y1 = y0 + height;

            Matrix4f pose = context.getGraphics().pose().last().pose();
            VertexConsumer bufferbuilder = context.getGraphics().bufferSource()
                    .getBuffer(MUIRenderTypes.guiTriangleStrip());
            v(pose, bufferbuilder, x0, y0, this.colorTL);
            v(pose, bufferbuilder, x1 - d, y0 + d, this.colorTR);
            v(pose, bufferbuilder, x1, y0, this.colorTR);
            v(pose, bufferbuilder, x1 - d, y1 - d, this.colorBR);
            v(pose, bufferbuilder, x1, y1, this.colorBR);
            v(pose, bufferbuilder, x0 + d, y1 - d, this.colorBL);
            v(pose, bufferbuilder, x0, y1, this.colorBL);
            v(pose, bufferbuilder, x0 + d, y0 + d, this.colorTL);
            v(pose, bufferbuilder, x0, y0, this.colorTL);
            v(pose, bufferbuilder, x1 - d, y0 + d, this.colorTR);
        }
    }

    private static void v(Matrix4f pose, VertexConsumer buffer, float x, float y, int c) {
        buffer.addVertex(pose, x, y, 0).setColor(c);
    }

    @Override
    public Rectangle interpolate(Rectangle start, Rectangle end, float t) {
        this.cornerRadius = Interpolations.lerp(start.cornerRadius, end.cornerRadius, t);
        this.cornerSegments = Interpolations.lerp(start.cornerSegments, end.cornerSegments, t);
        this.colorTL = Color.lerp(start.colorTL, end.colorTL, t);
        this.colorTR = Color.lerp(start.colorTR, end.colorTR, t);
        this.colorBL = Color.lerp(start.colorBL, end.colorBL, t);
        this.colorBR = Color.lerp(start.colorBR, end.colorBR, t);
        return this;
    }

    @Override
    public Rectangle copyOrImmutable() {
        return new Rectangle()
                .color(this.colorTL, this.colorTR, this.colorBL, this.colorBR)
                .cornerRadius(this.cornerRadius)
                .cornerSegments(this.cornerSegments)
                .canApplyTheme(this.canApplyTheme);
    }

    @Override
    public String getTypeName() {
        return "rectangle";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Rectangle rectangle)) return false;

        return cornerRadius == rectangle.cornerRadius && colorTL == rectangle.colorTL && colorTR == rectangle.colorTR &&
                colorBL == rectangle.colorBL && colorBR == rectangle.colorBR && cornerSegments == rectangle.cornerSegments &&
                Float.compare(borderThickness, rectangle.borderThickness) == 0 && canApplyTheme == rectangle.canApplyTheme;
    }

    @Override
    public int hashCode() {
        int result = cornerRadius;
        result = 31 * result + colorTL;
        result = 31 * result + colorTR;
        result = 31 * result + colorBL;
        result = 31 * result + colorBR;
        result = 31 * result + cornerSegments;
        result = 31 * result + Float.hashCode(borderThickness);
        result = 31 * result + Boolean.hashCode(canApplyTheme);
        return result;
    }
}
