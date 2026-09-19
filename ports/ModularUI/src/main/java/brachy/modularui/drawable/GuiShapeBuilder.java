package brachy.modularui.drawable;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

/** Records colored GUI geometry during extraction, before Minecraft draws the frame. */
public final class GuiShapeBuilder implements VertexConsumer {
    private final List<GuiShapeRenderState.Vertex> vertices = new ArrayList<>();
    private final GuiShapeRenderState.Topology topology;

    public GuiShapeBuilder(GuiShapeRenderState.Topology topology) {
        this.topology = topology;
    }

    public void submit(GuiGraphicsExtractor graphics) {
        GuiShapeRenderState.submit(graphics, vertices, topology);
        vertices.clear();
    }

    @Override
    public GuiShapeBuilder addVertex(float x, float y, float z) {
        vertices.add(new GuiShapeRenderState.Vertex(x, y, 0xFFFFFFFF));
        return this;
    }

    @Override
    public GuiShapeBuilder setColor(int color) {
        var previous = vertices.getLast();
        vertices.set(vertices.size() - 1, new GuiShapeRenderState.Vertex(previous.x(), previous.y(), color));
        return this;
    }

    @Override
    public GuiShapeBuilder setColor(int r, int g, int b, int a) {
        return setColor((a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        throw new UnsupportedOperationException("Use a textured GUI render state for UV geometry");
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        throw new UnsupportedOperationException("Colored GUI geometry has no overlay");
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        throw new UnsupportedOperationException("Colored GUI geometry has no lightmap");
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        throw new UnsupportedOperationException("Colored GUI geometry has no normals");
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        throw new UnsupportedOperationException("GUI lines must be expanded into quads");
    }
}
