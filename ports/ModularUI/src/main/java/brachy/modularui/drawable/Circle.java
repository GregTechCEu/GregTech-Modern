package brachy.modularui.drawable;

import brachy.modularui.animation.IAnimatable;
import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.Interpolations;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import com.mojang.serialization.Codec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@ToString
@Accessors(fluent = true, chain = true)
public class Circle implements IDrawable, IAnimatable<Circle> {

    public static final MutableObjectCodec<Circle> CODEC = MutableObjectCodec.drawableBuilder(Circle::new)
            .addOpt("colorInner", Circle::colorInner, Circle::colorInner, Codec.INT, 0).alias("color")
            .addOpt("colorOuter", Circle::colorOuter, Circle::colorOuter, Codec.INT, 0).alias("color")
            .addOpt("segments", Circle::segments, Circle::segments, Codec.INT, 40)
            .build();

    @Getter
    @Setter
    private int colorInner, colorOuter, segments;

    public Circle() {
        this.colorInner = 0;
        this.colorOuter = 0;
        this.segments = 40;
    }

    public Circle color(int inner, int outer) {
        this.colorInner = inner;
        this.colorOuter = outer;
        return this;
    }

    public Circle color(int color) {
        return color(color, color);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x0, int y0, int width, int height, WidgetTheme widgetTheme) {
        applyColor(widgetTheme.getColor());
        GuiDraw.drawEllipse(context.getGraphics(), x0, y0, width, height,
                this.colorInner, this.colorOuter, this.segments);
    }

    @Override
    public Circle interpolate(Circle start, Circle end, float t) {
        this.colorInner = Color.lerp(start.colorInner, end.colorInner, t);
        this.colorOuter = Color.lerp(start.colorOuter, end.colorOuter, t);
        this.segments = Interpolations.lerp(start.segments, end.segments, t);
        return this;
    }

    @Override
    public Circle copyOrImmutable() {
        return new Circle()
                .color(this.colorInner, this.colorOuter)
                .segments(this.segments);
    }

    @Override
    public String getTypeName() {
        return "circle";
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Circle circle)) return false;

        return colorInner == circle.colorInner && colorOuter == circle.colorOuter && segments == circle.segments;
    }

    @Override
    public int hashCode() {
        int result = colorInner;
        result = 31 * result + colorOuter;
        result = 31 * result + segments;
        return result;
    }
}
