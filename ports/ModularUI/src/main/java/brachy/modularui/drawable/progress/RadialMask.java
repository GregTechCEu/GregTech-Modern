package brachy.modularui.drawable.progress;

import brachy.modularui.drawable.GuiShapeRenderState.Vertex;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A top-starting radial sweep over a rectangle, expressed as a triangle fan. */
public final class RadialMask {
    private RadialMask() {}

    public static List<Vertex> vertices(float progress, float x, float y, float width, float height, boolean clockwise) {
        if (!Float.isFinite(progress) || progress <= 0 || width <= 0 || height <= 0) return List.of();
        double angle = Math.min(progress, 1) * Math.PI * 2;
        float halfWidth = width / 2, halfHeight = height / 2;
        float cx = x + halfWidth, cy = y + halfHeight;
        List<Vertex> edge = new ArrayList<>();
        edge.add(new Vertex(cx, y, -1));
        for (int corner = 0; corner < 4; corner++) {
            double cornerAngle = Math.PI / 4 + corner * Math.PI / 2;
            if (cornerAngle < angle) edge.add(boundary(cornerAngle, cx, cy, halfWidth, halfHeight, clockwise));
        }
        edge.add(boundary(angle, cx, cy, halfWidth, halfHeight, clockwise));
        // Match GUI rectangle winding in both sweep directions.
        if (clockwise) Collections.reverse(edge);
        List<Vertex> fan = new ArrayList<>();
        fan.add(new Vertex(cx, cy, -1));
        fan.addAll(edge);
        return List.copyOf(fan);
    }

    private static Vertex boundary(double angle, float cx, float cy, float rx, float ry, boolean clockwise) {
        double dx = Math.sin(angle) * (clockwise ? 1 : -1), dy = -Math.cos(angle);
        double divisor = Math.max(Math.abs(dx), Math.abs(dy));
        return new Vertex(cx + (float) (dx / divisor) * rx, cy + (float) (dy / divisor) * ry, -1);
    }
}
