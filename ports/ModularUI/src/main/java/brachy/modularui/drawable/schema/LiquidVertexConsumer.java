package brachy.modularui.drawable.schema;

import net.minecraft.core.SectionPos;
import com.mojang.blaze3d.vertex.VertexConsumer;

/** Offsets every fluid vertex from section-local to world coordinates, including chained writes. */
public final class LiquidVertexConsumer implements VertexConsumer {
    private final VertexConsumer delegate;
    private final SectionPos sectionPos;

    public LiquidVertexConsumer(VertexConsumer delegate, SectionPos sectionPos) {
        this.delegate = delegate;
        this.sectionPos = sectionPos;
    }

    @Override public VertexConsumer addVertex(float x, float y, float z) {
        delegate.addVertex(x + sectionPos.minBlockX(), y + sectionPos.minBlockY(), z + sectionPos.minBlockZ());
        return this;
    }
    @Override public VertexConsumer setColor(int r, int g, int b, int a) { delegate.setColor(r, g, b, a); return this; }
    @Override public VertexConsumer setColor(int color) { delegate.setColor(color); return this; }
    @Override public VertexConsumer setUv(float u, float v) { delegate.setUv(u, v); return this; }
    @Override public VertexConsumer setUv1(int u, int v) { delegate.setUv1(u, v); return this; }
    @Override public VertexConsumer setUv2(int u, int v) { delegate.setUv2(u, v); return this; }
    @Override public VertexConsumer setNormal(float x, float y, float z) { delegate.setNormal(x, y, z); return this; }
    @Override public VertexConsumer setLineWidth(float width) { delegate.setLineWidth(width); return this; }
}
