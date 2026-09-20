package brachy.modularui.drawable.schema;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.rendertype.RenderType;
import java.util.ArrayList;
import java.util.List;

/** CPU-owned structure geometry; snapshots never retain a mutable tessellation buffer. */
public final class SchemaGeometry {
    private SchemaGeometry() {}

    public record Layer(RenderType renderType, List<Vertex> vertices) {
        public Layer { vertices = List.copyOf(vertices); }
    }

    public record Vertex(float x, float y, float z, int color, float u, float v,
                         int overlay, int light, float normalX, float normalY, float normalZ, float lineWidth) {
        public void emit(VertexConsumer consumer, PoseStack.Pose pose) {
            consumer.addVertex(pose, x, y, z).setColor(color).setUv(u, v)
                    .setOverlay(overlay).setLight(light).setNormal(pose, normalX, normalY, normalZ)
                    .setLineWidth(lineWidth);
        }
    }

    /** VertexConsumer adapters are also used by Minecraft's fluid and baked-quad tessellators. */
    public static final class Builder implements VertexConsumer {
        private final List<Vertex> vertices = new ArrayList<>();
        private float offsetX, offsetY, offsetZ;
        private float x, y, z, u, v, nx, ny, nz, width = 1;
        private int color = -1, overlay, light;
        private boolean pending;

        public Builder offset(float x, float y, float z) {
            offsetX = x;
            offsetY = y;
            offsetZ = z;
            return this;
        }

        @Override public Builder addVertex(float x, float y, float z) {
            flush();
            this.x = x + offsetX;
            this.y = y + offsetY;
            this.z = z + offsetZ;
            u = v = nx = ny = nz = 0;
            color = -1;
            overlay = light = 0;
            width = 1;
            pending = true;
            return this;
        }

        private void requireVertex() {
            if (!pending) throw new IllegalStateException("Add a vertex before setting its attributes");
        }
        @Override public Builder setColor(int r, int g, int b, int a) {
            return setColor((a & 255) << 24 | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
        }
        @Override public Builder setColor(int value) { requireVertex(); color = value; return this; }
        @Override public Builder setUv(float u, float v) { requireVertex(); this.u = u; this.v = v; return this; }
        @Override public Builder setUv1(int u, int v) { requireVertex(); overlay = (u & 65535) | (v & 65535) << 16; return this; }
        @Override public Builder setUv2(int u, int v) { requireVertex(); light = (u & 65535) | (v & 65535) << 16; return this; }
        @Override public Builder setNormal(float x, float y, float z) { requireVertex(); nx = x; ny = y; nz = z; return this; }
        @Override public Builder setLineWidth(float value) { requireVertex(); width = value; return this; }

        private void flush() {
            if (!pending) return;
            vertices.add(new Vertex(x, y, z, color, u, v, overlay, light, nx, ny, nz, width));
            pending = false;
        }

        public List<Vertex> build() {
            flush();
            return List.copyOf(vertices);
        }
    }
}
