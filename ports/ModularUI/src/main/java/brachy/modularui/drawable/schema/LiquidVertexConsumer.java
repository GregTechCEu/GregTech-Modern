package brachy.modularui.drawable.schema;

import org.jspecify.annotations.NullMarked;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.SectionPos;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import org.joml.Matrix4f;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * The only purpose of this vertex consumer proxy is to transform vertex positions emitted by the
 * {@link net.minecraft.client.renderer.block.LiquidBlockRenderer} into absolute coordinates. The renderer assumes it is
 * being called in the context of tessellating a chunk section (16x16x16) and emits corresponding coordinates, while we
 * batch all visible chunks in schemas together.
 */
@ParametersAreNonnullByDefault
@NullMarked
public class LiquidVertexConsumer implements VertexConsumer {

    private final VertexConsumer delegate;
    private final SectionPos sectionPos;

    public LiquidVertexConsumer(VertexConsumer delegate, SectionPos sectionPos) {
        this.delegate = delegate;
        this.sectionPos = sectionPos;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        x += sectionPos.getX() * SectionPos.SECTION_SIZE;
        y += sectionPos.getY() * SectionPos.SECTION_SIZE;
        z += sectionPos.getZ() * SectionPos.SECTION_SIZE;

        return delegate.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return delegate.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return delegate.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return delegate.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return delegate.setNormal(x, y, z);
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float texU,
                          float texV, int overlayUV, int lightmapUV, float normalX, float normalY, float normalZ) {
        delegate.addVertex(x, y, z, color, texU, texV, overlayUV, lightmapUV, normalX, normalY, normalZ);
    }

    @Override
    public VertexConsumer setLight(int lightmapUV) {
        return delegate.setLight(lightmapUV);
    }

    @Override
    public VertexConsumer setOverlay(int overlayUV) {
        return delegate.setOverlay(overlayUV);
    }

    @Override
    public VertexConsumer setColor(float red, float green, float blue, float alpha) {
        return delegate.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setColor(int i) {
        return delegate.setColor(i);
    }

    @Override
    public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float red, float green, float blue, float alpha,
                            int combinedLight, int combinedOverlay) {
        delegate.putBulkData(poseEntry, quad, red, green, blue, alpha, combinedLight, combinedOverlay);
    }

    @Override
    public void putBulkData(PoseStack.Pose poseEntry, BakedQuad quad, float[] brightness,
                            float red, float green, float blue, float alpha,
                            int[] combinedLights, int combinedOverlay, boolean mulColor) {
        delegate.putBulkData(poseEntry, quad, brightness, red, green, blue, alpha, combinedLights, combinedOverlay, mulColor);
    }

    @Override
    public VertexConsumer addVertex(Matrix4f matrix4f, float f, float g, float h) {
        return delegate.addVertex(matrix4f, f, g, h);
    }

    @Override
    public VertexConsumer setNormal(PoseStack.Pose pose, float normalX, float normalY, float normalZ) {
        return delegate.setNormal(pose, normalX, normalY, normalZ);
    }
}
