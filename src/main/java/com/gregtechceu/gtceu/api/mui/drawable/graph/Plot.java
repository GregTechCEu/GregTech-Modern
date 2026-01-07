package com.gregtechceu.gtceu.api.mui.drawable.graph;

import com.gregtechceu.gtceu.api.mui.base.GuiAxis;
import com.gregtechceu.gtceu.api.mui.drawable.GuiDraw;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.api.mui.utils.DAM;
import com.gregtechceu.gtceu.api.mui.utils.Interpolations;
import com.gregtechceu.gtceu.client.renderer.GTRenderTypes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;

import com.mojang.blaze3d.systems.RenderSystem;
import lombok.Getter;

public class Plot {

    public static final int[] DEFAULT_PLOT_COLORS = {
            Color.BLUE_ACCENT.main,
            Color.ORANGE_ACCENT.darker(0),
            Color.GREEN.main,
            Color.RED.main,
            Color.DEEP_PURPLE_ACCENT.main,
            Color.BROWN.main,
            Color.TEAL.main,
            Color.LIME.main
    };

    double[] xs = DAM.EMPTY;
    double[] ys = DAM.EMPTY;
    @Getter
    float thickness = 1f;
    boolean defaultColor = true;
    @Getter
    int color;

    private float[] vertexBuffer; // screen coords need to be way less accurate than graph coords, so float is fine
    private boolean dirty = true;

    public void redraw() {
        this.dirty = true;
    }

    private void redraw(GraphView view) {
        long time = System.nanoTime();
        float dHalf = thickness * 0.5f;

        int n = xs.length * 4;
        this.vertexBuffer = new float[n];
        int vertexIndex = 0;

        // first only calculate the start point vertices
        // they are dependent on the first and second point
        float x0 = view.g2sX(xs[0]);
        float y0 = view.g2sY(ys[0]);
        float x1 = view.g2sX(xs[1]);
        float y1 = view.g2sY(ys[1]);
        // last pos
        float lx = x0;
        float ly = y0;

        float dx = x1 - x0;
        float dy = y1 - y0;
        float len = Mth.sqrt(dx * dx + dy * dy);
        if (len == 0) throw new IllegalArgumentException("Graph can't handle the same point back to back!");
        dx /= len;
        dy /= len;
        // perpendicular
        float px = -dy;
        float py = dx;
        // last perpendicular thickness offset
        float lpox = px * dHalf;
        float lpoy = py * dHalf;

        vertexIndex = storePoints(vertexIndex, view, lx, ly, lpox, lpoy);

        // calculate all points except start and endpoint
        // these depend on both their neighbors
        for (int i = 1; i < xs.length - 1; i++) {
            x0 = view.g2sX(xs[i]);
            y0 = view.g2sY(ys[i]);
            x1 = view.g2sX(xs[i + 1]);
            y1 = view.g2sY(ys[i + 1]);

            dx = x1 - x0;
            dy = y1 - y0;
            len = Mth.sqrt(dx * dx + dy * dy);
            if (len == 0) continue;
            dx /= len;
            dy /= len;
            // perpendicular
            px = -dy;
            py = dx;
            // perpendicular thickness offset
            float pox = px * dHalf;
            float poy = py * dHalf;
            float ox, oy;
            if (pox == lpox && poy == lpoy) { // linear
                ox = pox;
                oy = poy;
            } else {
                // get the average offset of this and the last point and of this and the next point
                ox = Interpolations.lerp(lpox, pox, 0.5f);
                oy = Interpolations.lerp(lpoy, poy, 0.5f);
                // normalize it
                len = Mth.sqrt(ox * ox + oy * oy);
                ox /= len;
                oy /= len;
                // angle between now offset vector and last perpendicular offset vector
                float cosAngle = (ox * lpox + oy * lpoy) / (1 * dHalf);
                // calc hypotenuse and use it to calculate the actual length of the offset vector
                float hypotenuse = this.thickness / cosAngle;
                ox *= hypotenuse * 0.5f;
                oy *= hypotenuse * 0.5f;
            }

            vertexIndex = storePoints(vertexIndex, view, x0, y0, ox, oy);

            lx = x0;
            ly = y0;
            lpox = pox;
            lpoy = poy;
        }

        // finally calculate endpoint
        // this depends on itself and the point before
        int last = this.xs.length - 1;
        x0 = lx;
        y0 = ly;
        x1 = view.g2sX(xs[last]);
        y1 = view.g2sY(ys[last]);

        dx = x1 - x0;
        dy = y1 - y0;
        len = Mth.sqrt(dx * dx + dy * dy);
        if (len == 0) throw new IllegalArgumentException("Graph can't handle the same point back to back!");
        dx /= len;
        dy /= len;
        // perpendicular
        px = -dy;
        py = dx;
        // last perpendicular thickness offset
        lpox = px * dHalf;
        lpoy = py * dHalf;

        storePoints(vertexIndex, view, x1, y1, lpox, lpoy);
        time = System.nanoTime() - time;
        // GTCEu.LOGGER.error("Calculating vertices from {} data points took {}s", xs.length,
        // FormattingUtil.formatNumberReadable(time));
    }

    private int storePoints(int index, GraphView view, float sx, float sy, float ox, float oy) {
        this.vertexBuffer[index++] = sx - ox;
        this.vertexBuffer[index++] = sy - oy;
        this.vertexBuffer[index++] = sx + ox;
        this.vertexBuffer[index++] = sy + oy;
        return index;
    }

    public void draw(GuiGraphics graphics, GraphView view) {
        if (xs.length == 0) return;
        if (xs.length == 1) {
            GuiDraw.drawRect(graphics, view.g2sX(xs[0]) - thickness / 2, view.g2sY(ys[0]) - thickness / 2, thickness,
                    thickness, color);
            return;
        }
        if (this.dirty) {
            redraw(view);
            this.dirty = false;
        }
        int r = Color.getRed(color);
        int g = Color.getGreen(color);
        int b = Color.getBlue(color);
        int a = Color.getAlpha(color);

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        var pose = graphics.pose().last().pose();
        var buffer = graphics.bufferSource().getBuffer(GTRenderTypes.guiTriangleStrip());
        long time = System.nanoTime();
        for (int i = 0; i < this.vertexBuffer.length; i += 2) {
            buffer.vertex(pose, this.vertexBuffer[i], this.vertexBuffer[i + 1], 0).color(r, g, b, a).endVertex();
            time = System.nanoTime() - time;
            // GTCEu.LOGGER.error("Drawing plot with {} points took {}s", xs.length,
            // FormattingUtil.formatNumberReadable(time));
        }
    }

    public double[] getX() {
        return xs;
    }

    public double[] getY() {
        return ys;
    }

    public double[] getData(GuiAxis axis) {
        return axis.isHorizontal() ? this.xs : this.ys;
    }

    public Plot data(double[] x, double[] y) {
        if (x.length != y.length) throw new IllegalArgumentException("X and Y must have the same length!");
        this.xs = x;
        this.ys = y;
        redraw();
        return this;
    }

    public Plot thickness(float thickness) {
        this.thickness = thickness;
        redraw();
        return this;
    }

    public Plot color(int color) {
        this.color = color;
        this.defaultColor = color == 0;
        return this;
    }
}
