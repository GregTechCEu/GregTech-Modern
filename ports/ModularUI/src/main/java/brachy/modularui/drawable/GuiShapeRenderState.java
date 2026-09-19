package brachy.modularui.drawable;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.ARGB;
import org.joml.Vector2f;
import org.joml.Matrix3x2fc;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Immutable, screen-space geometry for the deferred GUI renderer. */
public record GuiShapeRenderState(List<Vertex> vertices, @Nullable ScreenRectangle scissorArea,
                                  @Nullable ScreenRectangle bounds) implements GuiElementRenderState {

    public GuiShapeRenderState {
        vertices = List.copyOf(vertices);
    }

    public record Vertex(float x, float y, int color) {}

    public enum Topology { QUADS, TRIANGLE_FAN, TRIANGLE_STRIP }

    public static void submit(GuiGraphicsExtractor graphics, List<Vertex> vertices, boolean triangleFan) {
        submit(graphics, vertices, triangleFan ? Topology.TRIANGLE_FAN : Topology.QUADS);
    }

    public static void submit(GuiGraphicsExtractor graphics, List<Vertex> vertices, Topology topology) {
        int tint = GuiTint.get();
        if (tint != -1) {
            vertices = vertices.stream().map(v -> new Vertex(v.x(), v.y(), ARGB.multiply(v.color(), tint))).toList();
        }
        GuiShapeRenderState state = extract(graphics.pose(), graphics.peekScissorStack(), vertices, topology);
        if (state != null) graphics.submitGuiElementRenderState(state);
    }

    public static @Nullable GuiShapeRenderState extract(Matrix3x2fc pose, @Nullable ScreenRectangle scissor,
                                                         List<Vertex> vertices, Topology topology) {
        if (vertices.isEmpty()) return null;
        if (topology == Topology.QUADS && vertices.size() % 4 != 0) {
            throw new IllegalArgumentException("GUI quad geometry must contain a multiple of four vertices");
        }
        if (vertices.size() < (topology == Topology.QUADS ? 4 : 3)) return null;
        List<Vertex> transformed = new ArrayList<>(vertices.size());
        float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY;
        float maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
        for (Vertex vertex : vertices) {
            Vector2f point = pose.transformPosition(vertex.x(), vertex.y(), new Vector2f());
            transformed.add(new Vertex(point.x, point.y, vertex.color()));
            minX = Math.min(minX, point.x);
            minY = Math.min(minY, point.y);
            maxX = Math.max(maxX, point.x);
            maxY = Math.max(maxY, point.y);
        }
        // Degenerate quads keep each triangle independent when the GUI renderer batches meshes.
        if (topology != Topology.QUADS) {
            List<Vertex> quads = new ArrayList<>((transformed.size() - 2) * 4);
            for (int i = 1; i < transformed.size() - 1; i++) {
                int first = topology == Topology.TRIANGLE_FAN ? 0 : i - 1;
                // Strip triangles alternate winding.
                quads.add(transformed.get(topology == Topology.TRIANGLE_STRIP && i % 2 == 0 ? i : first));
                quads.add(transformed.get(topology == Topology.TRIANGLE_STRIP && i % 2 == 0 ? first : i));
                quads.add(transformed.get(i + 1));
                quads.add(transformed.get(i + 1));
            }
            transformed = quads;
        }
        ScreenRectangle bounds = new ScreenRectangle((int) Math.floor(minX), (int) Math.floor(minY),
                (int) Math.ceil(maxX) - (int) Math.floor(minX),
                (int) Math.ceil(maxY) - (int) Math.floor(minY));
        if (scissor != null) bounds = bounds.intersection(scissor);
        return bounds == null ? null : new GuiShapeRenderState(transformed, scissor, bounds);
    }

    @Override
    public void buildVertices(VertexConsumer consumer) {
        for (Vertex vertex : vertices) consumer.addVertex(vertex.x(), vertex.y(), 0).setColor(vertex.color());
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI;
    }

    @Override
    public TextureSetup textureSetup() {
        return TextureSetup.noTexture();
    }
}
