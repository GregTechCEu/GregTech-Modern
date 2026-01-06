package com.gregtechceu.gtceu.api.mui.drawable.graph;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.FloatArrayMath;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import java.text.DecimalFormat;

@ApiStatus.Experimental
public class GraphAxis {

    private static final TextRenderer textRenderer = new TextRenderer();
    private static final float TICK_LABEL_SCALE = 0.4f;
    private static final float AXIS_LABEL_SCALE = 1f;
    private static final float TICK_LABEL_OFFSET = 2f;
    private static final float AXIS_LABEL_OFFSET = 3f;

    @Getter
    public final GuiAxis axis;

    public float[] majorTicks = new float[8];
    public float[] minorTicks = new float[16];
    public TextRenderer.Line[] tickLabels = new TextRenderer.Line[8];
    private float maxLabelWidth = 0;
    @Getter
    public MajorTickFinder majorTickFinder = new AutoMajorTickFinder(10);
    @Getter
    public MinorTickFinder minorTickFinder = new AutoMinorTickFinder(1);
    @Getter
    public String label;
    @Getter
    public float min, max;
    public boolean autoLimits = true;
    public float[] data;

    public GraphAxis(GuiAxis axis) {
        this.axis = axis;
    }

    void compute() {
        if (this.autoLimits) {
            this.min = FloatArrayMath.min(this.data);
            this.max = FloatArrayMath.max(this.data);
        }
        this.majorTicks = this.majorTickFinder.find(this.min, this.max, this.majorTicks);
        this.minorTicks = this.minorTickFinder.find(this.min, this.max, this.majorTicks, this.minorTicks);

        if (this.tickLabels.length < this.majorTicks.length) {
            this.tickLabels = new TextRenderer.Line[this.majorTicks.length];
        }
        textRenderer.setScale(TICK_LABEL_SCALE);
        this.maxLabelWidth = 0;
        float maxDiff = FloatArrayMath.max(FloatArrayMath.diff(this.majorTicks));
        int significantPlaces = (int) Math.abs(Math.log10(maxDiff)) + 1;
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(significantPlaces);
        for (int i = 0; i < this.tickLabels.length; i++) {
            if (Float.isNaN(this.majorTicks[i])) break;
            this.tickLabels[i] = textRenderer.line(Component.literal(format.format(this.majorTicks[i])).getVisualOrderText());
            if (this.tickLabels[i].getWidth() > this.maxLabelWidth) {
                this.maxLabelWidth = this.tickLabels[i].getWidth();
            }
        }
    }

    void applyPadding(GraphView graphView) {
        textRenderer.setScale(TICK_LABEL_SCALE);
        if (this.axis.isHorizontal()) {
            graphView.sy1 -= textRenderer.getFontHeight() + TICK_LABEL_OFFSET;
            if (this.label != null) {
                textRenderer.setScale(AXIS_LABEL_SCALE);
                graphView.sy1 -= textRenderer.getFontHeight() + AXIS_LABEL_OFFSET;
            }
        } else {
            float off = this.maxLabelWidth + TICK_LABEL_OFFSET;
            if (this.label != null) {
                textRenderer.setScale(AXIS_LABEL_SCALE);
                off += textRenderer.getFontHeight() + AXIS_LABEL_OFFSET;
            }
            graphView.sx0 += off;
            graphView.sx1 -= off;
        }
    }

    void drawGridLines(GuiGraphics graphics, VertexConsumer buffer, GraphView view, GraphAxis other, boolean major, float d, int r, int g, int b, int a) {
        float[] pos = major ? this.majorTicks : this.minorTicks;
        float dHalf = d / 2;
        if (axis.isHorizontal()) {
            float otherMin = view.transformYGraphToScreen(other.max);
            float otherMax = view.transformYGraphToScreen(other.min);
            drawLinesOnHorizontal(graphics, buffer, view, pos, dHalf, otherMin, otherMax, r, g, b, a);
        } else {
            float otherMin = view.transformXGraphToScreen(other.min);
            float otherMax = view.transformXGraphToScreen(other.max);
            drawLinesOnVertical(graphics, buffer, view, pos, dHalf, otherMin, otherMax, r, g, b, a);
        }
    }

    void drawTicks(GuiGraphics graphics, VertexConsumer buffer, GraphView view, GraphAxis other, boolean major, float thickness, float length, int r, int g, int b, int a) {
        float[] pos = major ? this.majorTicks : this.minorTicks;
        float dHalf = thickness / 2;
        if (axis.isHorizontal()) {
            float otherMin = view.transformYGraphToScreen(other.min);
            drawLinesOnHorizontal(graphics, buffer, view, pos, dHalf, otherMin - length, otherMin, r, g, b, a);
        } else {
            float otherMin = view.transformXGraphToScreen(other.min);
            drawLinesOnVertical(graphics, buffer, view, pos, dHalf, otherMin, otherMin + length, r, g, b, a);
        }
    }

    private void drawLinesOnHorizontal(GuiGraphics graphics, VertexConsumer buffer, GraphView view, float[] pos, float dHalf,
                                       float crossLow, float crossHigh, int r, int g, int b, int a) {
        for (float p : pos) {
            if (Float.isNaN(p)) break;
            if (p < min || p > max) continue;

            p = view.transformXGraphToScreen(p);

            float x0 = p - dHalf;
            float x1 = p + dHalf;
            GuiDraw.drawRectRaw(buffer, graphics.pose().last().pose(), x0, crossLow, x1, crossHigh, r, g, b, a);
        }
    }

    private void drawLinesOnVertical(GuiGraphics graphics, VertexConsumer buffer, GraphView view, float[] pos, float dHalf,
                                     float crossLow, float crossHigh, int r, int g, int b, int a) {
        for (float p : pos) {
            if (Float.isNaN(p)) break;
            if (p < min || p > max) continue;

            p = view.transformYGraphToScreen(p);

            float y0 = p - dHalf;
            float y1 = p + dHalf;
            GuiDraw.drawRectRaw(buffer, graphics.pose().last().pose(), crossLow, y0, crossHigh, y1, r, g, b, a);
        }
    }

    void drawLabels(GraphView view, GraphAxis other, GuiGraphics graphics) {
        textRenderer.setHardWrapOnBorder(false);
        if (axis.isHorizontal()) {
            textRenderer.setScale(TICK_LABEL_SCALE);
            textRenderer.setAlignment(Alignment.TopCenter, 100);
            float y = view.transformYGraphToScreen(other.min) + TICK_LABEL_OFFSET;
            for (int i = 0; i < this.majorTicks.length; i++) {
                float pos = this.majorTicks[i];
                if (Float.isNaN(pos)) break;
                if (pos < min || pos > max) continue;
                textRenderer.setPos((int) (view.transformXGraphToScreen(pos) - 50), (int) y);
                textRenderer.drawSimple(graphics, this.tickLabels[i].getText());
            }
        } else {
            textRenderer.setScale(TICK_LABEL_SCALE);
            textRenderer.setAlignment(Alignment.CenterRight, this.maxLabelWidth, 20);
            float x = view.transformXGraphToScreen(other.min) - TICK_LABEL_OFFSET - this.maxLabelWidth;
            for (int i = 0; i < this.majorTicks.length; i++) {
                float pos = this.majorTicks[i];
                if (Float.isNaN(pos)) break;
                if (pos < min || pos > max) continue;
                textRenderer.setPos((int) x, (int) (view.transformYGraphToScreen(pos) - 10));
                textRenderer.drawSimple(graphics, this.tickLabels[i].getText());
            }
        }
    }
}
