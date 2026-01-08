package com.gregtechceu.gtceu.api.mui.drawable.graph;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.drawable.text.TextRenderer;
import com.gregtechceu.gtceu.api.mui.utils.Alignment;
import com.gregtechceu.gtceu.api.mui.utils.DAM;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.Getter;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix4f;

import java.text.DecimalFormat;
import java.util.List;

@ApiStatus.Experimental
public class GraphAxis {

    private static final TextRenderer textRenderer = new TextRenderer();
    private static final float TICK_LABEL_SCALE = 0.4f;
    private static final float AXIS_LABEL_SCALE = 1f;
    private static final float TICK_LABEL_OFFSET = 2f;
    private static final float AXIS_LABEL_OFFSET = 3f;

    @Getter
    public final GuiAxis axis;

    public double[] majorTicks = new double[8];
    public double[] minorTicks = new double[16];
    public TextRenderer.Line[] tickLabels = new TextRenderer.Line[8];
    private float maxLabelWidth = 0;
    @Getter
    public MajorTickFinder majorTickFinder = new AutoMajorTickFinder(true);
    @Getter
    public MinorTickFinder minorTickFinder = new AutoMinorTickFinder(2);
    @Getter
    public String label;
    @Getter
    public double min, max;
    public boolean autoLimits = true;
    public float[] data;

    public GraphAxis(GuiAxis axis) {
        this.axis = axis;
    }

    void compute(List<Plot> plots) {
        if (this.autoLimits) {
            if (plots.isEmpty()) {
                this.min = 0;
                this.max = 0;
            } else if (plots.size() == 1) {
                this.min = DAM.min(plots.get(0).getData(this.axis));
                this.max = DAM.max(plots.get(0).getData(this.axis));
            } else {
                double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
                for (Plot plot : plots) {
                    double m = DAM.min(plot.getData(this.axis));
                    if (m < min) min = m;
                    m = DAM.max(plot.getData(this.axis));
                    if (m > max) max = m;
                }
                this.min = min;
                this.max = max;
            }
            if (this.axis.isVertical()) {
                double padding = (this.max - this.min) * 0.05f;
                this.max += padding;
                this.min -= padding;
            }
        }
        if (this.majorTickFinder instanceof AutoMajorTickFinder tickFinder && tickFinder.isAutoAdjust()) {
            tickFinder.calculateAutoTickMultiple(this.min, this.max);
        }
        this.majorTicks = this.majorTickFinder.find(this.min, this.max, this.majorTicks);
        this.minorTicks = this.minorTickFinder.find(this.min, this.max, this.majorTicks, this.minorTicks);

        if (this.tickLabels.length < this.majorTicks.length) {
            this.tickLabels = new TextRenderer.Line[this.majorTicks.length];
        }
        textRenderer.setScale(TICK_LABEL_SCALE);
        this.maxLabelWidth = 0;
        double maxDiff = DAM.max(DAM.diff(this.majorTicks));
        int significantPlaces = (int) Math.abs(Math.log10(maxDiff)) + 2;
        DecimalFormat format = new DecimalFormat();
        format.setMaximumFractionDigits(significantPlaces);
        for (int i = 0; i < this.tickLabels.length; i++) {
            if (Double.isNaN(this.majorTicks[i])) break;
            this.tickLabels[i] = textRenderer
                    .line(Component.literal(format.format(this.majorTicks[i])).getVisualOrderText());
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
        }
    }

    void drawGridLines(Matrix4f pose, VertexConsumer buffer, GraphView view, GraphAxis other, boolean major, float d,
                       int r, int g, int b, int a) {
        double[] pos = major ? this.majorTicks : this.minorTicks;
        float dHalf = d / 2;
        if (axis.isHorizontal()) {
            float otherMin = view.g2sY(other.max);
            float otherMax = view.g2sY(other.min);
            drawLinesOnHorizontal(pose, buffer, view, pos, dHalf, otherMin, otherMax, r, g, b, a);
        } else {
            float otherMin = view.g2sX(other.min);
            float otherMax = view.g2sX(other.max);
            drawLinesOnVertical(pose, buffer, view, pos, dHalf, otherMin, otherMax, r, g, b, a);
        }
    }

    void drawTicks(Matrix4f pose, VertexConsumer buffer, GraphView view, GraphAxis other, boolean major,
                   float thickness, float length, int r, int g, int b, int a) {
        double[] pos = major ? this.majorTicks : this.minorTicks;
        float dHalf = thickness / 2;
        if (axis.isHorizontal()) {
            float otherMin = view.g2sY(other.min);
            drawLinesOnHorizontal(pose, buffer, view, pos, dHalf, otherMin - length, otherMin, r, g, b, a);
        } else {
            float otherMin = view.g2sX(other.min);
            drawLinesOnVertical(pose, buffer, view, pos, dHalf, otherMin, otherMin + length, r, g, b, a);
        }
    }

    private void drawLinesOnHorizontal(Matrix4f pose, VertexConsumer buffer, GraphView view, double[] pos, float dHalf,
                                       float crossLow, float crossHigh, int r, int g, int b, int a) {
        for (double p : pos) {
            if (Double.isNaN(p)) break;
            if (p < min || p > max) continue;

            float fp = view.g2sX(p);

            float x0 = fp - dHalf;
            float x1 = fp + dHalf;
            GuiDraw.drawRectRaw(buffer, pose, x0, crossLow, x1, crossHigh, r, g, b, a);
        }
    }

    private void drawLinesOnVertical(Matrix4f pose, VertexConsumer buffer, GraphView view, double[] pos, float dHalf,
                                     float crossLow, float crossHigh, int r, int g, int b, int a) {
        for (double p : pos) {
            if (Double.isNaN(p)) break;
            if (p < min || p > max) continue;

            float fp = view.g2sY(p);

            float y0 = fp - dHalf;
            float y1 = fp + dHalf;
            GuiDraw.drawRectRaw(buffer, pose, crossLow, y0, crossHigh, y1, r, g, b, a);
        }
    }

    void drawLabels(GraphView view, GraphAxis other, GuiGraphics graphics) {
        textRenderer.setHardWrapOnBorder(false);
        if (axis.isHorizontal()) {
            textRenderer.setScale(TICK_LABEL_SCALE);
            textRenderer.setAlignment(Alignment.TopCenter, 100);
            float y = view.g2sY(other.min) + TICK_LABEL_OFFSET;
            for (int i = 0; i < this.majorTicks.length; i++) {
                double pos = this.majorTicks[i];
                if (Double.isNaN(pos)) break;
                if (pos < min || pos > max) continue;
                textRenderer.setPos((int) (view.g2sX(pos) - 50), (int) y);
                textRenderer.drawSimple(graphics, this.tickLabels[i].getText());
            }
        } else {
            textRenderer.setScale(TICK_LABEL_SCALE);
            textRenderer.setAlignment(Alignment.CenterRight, this.maxLabelWidth, 20);
            float x = view.g2sX(other.min) - TICK_LABEL_OFFSET - this.maxLabelWidth;
            for (int i = 0; i < this.majorTicks.length; i++) {
                double pos = this.majorTicks[i];
                if (Double.isNaN(pos)) break;
                if (pos < min || pos > max) continue;
                textRenderer.setPos((int) x, (int) (view.g2sY(pos) - 10));
                textRenderer.drawSimple(graphics, this.tickLabels[i].getText());
            }
        }
    }
}
