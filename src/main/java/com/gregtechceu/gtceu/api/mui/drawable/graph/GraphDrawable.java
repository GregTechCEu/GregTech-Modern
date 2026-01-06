package com.gregtechceu.gtceu.api.mui.drawable.graph;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.client.mui.screen.viewport.GuiContext;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class GraphDrawable implements IDrawable {

    private final GraphView view = new GraphView();
    //private IDrawable background;
    private int backgroundColor = Color.WHITE.main;

    private float lineWidth = 2;
    private int lineColor = Color.BLUE_ACCENT.main;

    private boolean borderTop = true, borderLeft = true, borderBottom = false, borderRight = false;

    private float gridLineWidth = 0.5f;
    private int gridLineColor = Color.withAlpha(Color.BLACK.main, 0.4f);
    private float minorGridLineWidth = 0.25f;
    private int minorGridLineColor = Color.withAlpha(Color.BLACK.main, 0.15f);

    @Getter
    private final GraphAxis x = new GraphAxis(GuiAxis.X), y = new GraphAxis(GuiAxis.Y);

    private float majorTickThickness = 0.5f, majorTickLength = 1f, minorTickThickness = 0.25f, minorTickLength = 0.5f;

    private boolean dirty = true;

    public void redraw() {
        this.dirty = true;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.view.setScreen(x, y, x + width, y + height) | compute()) {
            this.x.applyPadding(this.view);
            this.y.applyPadding(this.view);
            this.view.setGraph(this.x.min, this.y.min, this.x.max, this.y.max);
        }

        if (this.backgroundColor != 0) {
            GuiDraw.drawRect(context.getGraphics(), this.view.sx0, this.view.sy0, this.view.sx1 - this.view.sx0, this.view.sy1 - this.view.sy0, this.backgroundColor);
        }
        var graphics = context.getGraphics();
        var buffer = graphics.bufferSource().getBuffer(RenderType.guiOverlay());
        drawGrid(context, buffer);
        drawData(context, this.view, this.x.data, this.y.data, this.lineWidth, this.lineColor);

        GuiDraw.drawBorderOutsideLTRB(graphics, this.view.sx0, this.view.sy0, this.view.sx1, this.view.sy1, 0.5f, Color.BLACK.main);
        this.x.drawLabels(this.view, this.y, graphics);
        this.y.drawLabels(this.view, this.x, graphics);
    }

    public void drawGrid(GuiContext context, VertexConsumer buffer) {
        if (this.minorGridLineWidth > 0) {
            int r = Color.getRed(this.minorGridLineColor);
            int g = Color.getGreen(this.minorGridLineColor);
            int b = Color.getBlue(this.minorGridLineColor);
            int a = Color.getAlpha(this.minorGridLineColor);
            this.x.drawGridLines(context.getGraphics(), buffer, this.view, this.y, false, this.minorGridLineWidth, r, g, b, a);
        }
        if (this.gridLineWidth > 0) {
            int r = Color.getRed(this.gridLineColor);
            int g = Color.getGreen(this.gridLineColor);
            int b = Color.getBlue(this.gridLineColor);
            int a = Color.getAlpha(this.gridLineColor);
            this.x.drawGridLines(context.getGraphics(), buffer, this.view, this.y, true, this.gridLineWidth, r, g, b, a);
            this.y.drawGridLines(context.getGraphics(), buffer, this.view, this.x, true, this.gridLineWidth, r, g, b, a);
        }

        this.x.drawTicks(context.getGraphics(), buffer, this.view, this.y, false, this.minorTickThickness, this.minorTickLength, 0, 0, 0, 0xFF);
        this.y.drawTicks(context.getGraphics(), buffer, this.view, this.x, false, this.minorTickThickness, this.minorTickLength, 0, 0, 0, 0xFF);
        this.x.drawTicks(context.getGraphics(), buffer, this.view, this.y, true, this.majorTickThickness, this.majorTickLength, 0, 0, 0, 0xFF);
        this.y.drawTicks(context.getGraphics(), buffer, this.view, this.x, true, this.majorTickThickness, this.majorTickLength, 0, 0, 0, 0xFF);
    }

    public static void drawData(GuiContext context, GraphView view, float[] xs, float[] ys, float d, int color) {
        int r = Color.getRed(color);
        int g = Color.getGreen(color);
        int b = Color.getBlue(color);
        int a = Color.getAlpha(color);

        var graphics = context.getGraphics();
        var buffer = graphics.bufferSource().getBuffer(GTRenderTypes.guiTriangleStrip());
        for (int i = 0; i < xs.length - 1; i++) {
            float x0 = view.transformXGraphToScreen(xs[i]);
            float y0 = view.transformYGraphToScreen(ys[i]);
            float x1 = view.transformXGraphToScreen(xs[i + 1]);
            float y1 = view.transformYGraphToScreen(ys[i + 1]);

            float dx = x1 - x0;
            float dy = y1 - y0;
            float len = Mth.sqrt(dx * dx + dy * dy);
            if (len == 0) continue;
            dx /= len;
            dy /= len;

            // perpendicular
            float px = -dy;
            float py = dx;
            // thickness offset
            float ox = px * (d * 0.5f);
            float oy = py * (d * 0.5f);

            buffer.vertex(x0 - ox, y0 - oy, 0).color(r, g, b, a).endVertex();
            buffer.vertex(x0 + ox, y0 + oy, 0).color(r, g, b, a).endVertex();
            buffer.vertex(x1 - ox, y1 - oy, 0).color(r, g, b, a).endVertex();
            buffer.vertex(x1 + ox, y1 + oy, 0).color(r, g, b, a).endVertex();
        }
    }

    private boolean compute() {
        if (!this.dirty) return false;
        this.dirty = false;
        this.x.compute();
        this.y.compute();
        return true;
    }

    public GraphDrawable data(float[] x, float[] y) {
        this.x.data = x;
        this.y.data = y;
        this.dirty |= this.x.autoLimits || this.y.autoLimits;
        return this;
    }

    public GraphDrawable xData(float[] x) {
        return data(x, this.y.data);
    }

    public GraphDrawable yData(float[] y) {
        return data(this.x.data, y);
    }

    public GraphDrawable autoXLim() {
        this.x.autoLimits = true;
        redraw();
        return this;
    }

    public GraphDrawable autoYLim() {
        this.y.autoLimits = true;
        redraw();
        return this;
    }

    public GraphDrawable xLim(float min, float max) {
        this.x.min = min;
        this.x.max = max;
        this.x.autoLimits = false;
        redraw();
        return this;
    }

    public GraphDrawable yLim(float min, float max) {
        this.y.min = min;
        this.y.max = max;
        this.y.autoLimits = false;
        redraw();
        return this;
    }

    public GraphDrawable majorTickStyle(float thickness, float length) {
        this.majorTickThickness = thickness;
        this.majorTickLength = length;
        return this;
    }

    public GraphDrawable minorTickStyle(float thickness, float length) {
        this.minorTickThickness = thickness;
        this.minorTickLength = length;
        return this;
    }

    public GraphDrawable xTickFinder(MajorTickFinder majorTickFinder, MinorTickFinder minorTickFinder) {
        this.x.majorTickFinder = majorTickFinder;
        this.x.minorTickFinder = minorTickFinder;
        redraw();
        return this;
    }

    public GraphDrawable yTickFinder(MajorTickFinder majorTickFinder, MinorTickFinder minorTickFinder) {
        this.y.majorTickFinder = majorTickFinder;
        this.y.minorTickFinder = minorTickFinder;
        redraw();
        return this;
    }

    public GraphDrawable xTickFinder(float majorMultiples, int minorTicksBetweenMajors) {
        return xTickFinder(new AutoMajorTickFinder(majorMultiples), new AutoMinorTickFinder(minorTicksBetweenMajors));
    }

    public GraphDrawable yTickFinder(float majorMultiples, int minorTicksBetweenMajors) {
        return yTickFinder(new AutoMajorTickFinder(majorMultiples), new AutoMinorTickFinder(minorTicksBetweenMajors));
    }

    public GraphDrawable backgroundColor(int color) {
        if (color != 0 && Color.getAlpha(color) == 0) {
            color = Color.withAlpha(color, 0xFF);
        }
        this.backgroundColor = color;
        return this;
    }

    public GraphDrawable lineThickness(float thickness) {
        this.lineWidth = thickness;
        return this;
    }

    public GraphDrawable lineColor(int color) {
        this.lineColor = color;
        return this;
    }

    public GraphDrawable majorGridStyle(float thickness, int color) {
        this.gridLineWidth = thickness;
        this.gridLineColor = color;
        return this;
    }

    public GraphDrawable minorGridStyle(float thickness, int color) {
        this.minorGridLineWidth = thickness;
        this.minorGridLineColor = color;
        return this;
    }
}
