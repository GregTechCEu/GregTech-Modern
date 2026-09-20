package brachy.modularui.drawable.schema;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.ArrayList;
import java.util.List;

/** CPU-owned geometry. No world objects or mutable vertex builders survive extraction. */
public final class SchemaGeometry {
    private SchemaGeometry() {}

    public record Vertex(float x, float y, float z, int color, float u, float v,
                         int overlayU, int overlayV, int lightU, int lightV,
                         float normalX, float normalY, float normalZ) {
        public void write(VertexConsumer consumer) {
            consumer.addVertex(x, y, z).setColor(color).setUv(u, v)
                    .setUv1(overlayU, overlayV).setUv2(lightU, lightV).setNormal(normalX, normalY, normalZ);
        }
        public void write(PoseStack.Pose pose, VertexConsumer consumer) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v)
                    .setUv1(overlayU, overlayV).setUv2(lightU, lightV).setNormal(pose, normalX, normalY, normalZ);
        }
    }

    public record Layer(RenderType type, List<Vertex> vertices) {
        public Layer { vertices = List.copyOf(vertices); }
    }

    public static final class Builder implements VertexConsumer {
        private final List<Vertex> vertices = new ArrayList<>();
        private float x, y, z, u, v, nx, ny, nz;
        private int color = -1, overlayU, overlayV = 10, lightU, lightV;
        private boolean pending;
        private float offsetX, offsetY, offsetZ;

        public Builder offset(float x, float y, float z) {
            finishVertex();
            offsetX = x;
            offsetY = y;
            offsetZ = z;
            return this;
        }

        private void finishVertex() {
            if (pending) vertices.add(new Vertex(x, y, z, color, u, v, overlayU, overlayV, lightU, lightV, nx, ny, nz));
            pending = false;
        }

        public List<Vertex> build() {
            finishVertex();
            if (vertices.size() % 4 != 0) throw new IllegalStateException("Scene geometry must contain complete quads");
            return List.copyOf(vertices);
        }

        @Override public Builder addVertex(float x, float y, float z) {
            finishVertex();
            this.x = x + offsetX;
            this.y = y + offsetY;
            this.z = z + offsetZ;
            color = -1;
            u = v = nx = nz = 0;
            ny = 1;
            overlayU = lightU = lightV = 0;
            overlayV = 10;
            pending = true;
            return this;
        }
        @Override public Builder setColor(int value) { color = value; return this; }
        @Override public Builder setColor(int r, int g, int b, int a) {
            return setColor((a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
        }
        @Override public Builder setUv(float u, float v) { this.u = u; this.v = v; return this; }
        @Override public Builder setUv1(int u, int v) { overlayU = u; overlayV = v; return this; }
        @Override public Builder setUv2(int u, int v) { lightU = u; lightV = v; return this; }
        @Override public Builder setNormal(float x, float y, float z) { nx = x; ny = y; nz = z; return this; }
        @Override public Builder setLineWidth(float width) {
            throw new UnsupportedOperationException("Scene lines must be expanded to quads");
        }
    }
}
