package com.gregtechceu.gtceu.api.mui.drawable;

import com.gregtechceu.gtceu.api.mui.animation.IAnimatable;
import com.gregtechceu.gtceu.api.mui.base.IJsonSerializable;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.Interpolations;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.utils.serialization.json.JsonHelper;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import com.google.gson.JsonObject;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(fluent = true, chain = true)
public class Circle implements IDrawable, IJsonSerializable<Circle>, IAnimatable<Circle> {

    @Setter
    private int colorInner, colorOuter, segments;

    public Circle() {
        this.colorInner = 0;
        this.colorOuter = 0;
        this.segments = 40;
    }

    public Circle setColorInner(int colorInner) {
        return colorInner(colorInner);
    }

    public Circle setColorOuter(int colorOuter) {
        return colorOuter(colorOuter);
    }

    public Circle setColor(int inner, int outer) {
        return color(inner, outer);
    }

    public Circle setSegments(int segments) {
        return segments(segments);
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
    public void loadFromJson(JsonObject json) {
        this.colorInner = JsonHelper.getColor(json, Color.WHITE.main, "colorInner", "color");
        this.colorOuter = JsonHelper.getColor(json, Color.WHITE.main, "colorOuter", "color");
        this.segments = JsonHelper.getInt(json, 40, "segments");
    }

    @Override
    public boolean saveToJson(JsonObject json) {
        json.addProperty("colorInner", this.colorInner);
        json.addProperty("colorOuter", this.colorOuter);
        json.addProperty("segments", this.segments);
        return true;
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
                .setColor(this.colorInner, this.colorOuter)
                .setSegments(this.segments);
    }
}
