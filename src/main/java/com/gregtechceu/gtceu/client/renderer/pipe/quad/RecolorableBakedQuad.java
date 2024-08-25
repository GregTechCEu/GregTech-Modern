package com.gregtechceu.gtceu.client.renderer.pipe.quad;

import com.gregtechceu.gtceu.client.renderer.pipe.util.ColorData;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.QuadTransformers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import lombok.Setter;
import lombok.experimental.Accessors;

import static net.minecraftforge.client.model.IQuadTransformer.*;

@MethodsReturnNonnullByDefault
@OnlyIn(Dist.CLIENT)
public class RecolorableBakedQuad extends BakedQuad {

    private final SpriteInformation spriteInformation;

    public RecolorableBakedQuad(int[] unpackedData, int tint, Direction orientation,
                                SpriteInformation texture, boolean applyDiffuseLighting, boolean hasAmbientOcclusion) {
        super(unpackedData, tint, orientation, texture.sprite(), applyDiffuseLighting, hasAmbientOcclusion);
        this.spriteInformation = texture;
    }

    public BakedQuad withColor(ColorData data) {
        if (!spriteInformation.colorable()) return this;
        int argb = data.colorsARGB()[spriteInformation.colorID()];
        return QuadTransformers.applyingColor(argb).process(this);
    }

    @Accessors(chain = true)
    public static class Builder implements VertexConsumer {

        private final int[] unpackedData;
        @Setter
        private int quadTint = -1;
        @Setter
        private Direction quadOrientation;
        @Setter
        private SpriteInformation texture;
        @Setter
        private boolean shade = true;
        @Setter
        private boolean hasAmbientOcclusion = true;

        private int vertices = 0;
        private int elements = 0;
        private boolean full = false;
        @Setter
        private boolean contractUVs = false;

        public Builder() {
            unpackedData = new int[STRIDE * 4];
        }

        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            int offset = vertices * STRIDE + POSITION;
            unpackedData[offset] = Float.floatToRawIntBits((float) x);
            unpackedData[offset + 1] = Float.floatToRawIntBits((float) y);
            unpackedData[offset + 2] = Float.floatToRawIntBits((float) z);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer color(int red, int green, int blue, int alpha) {
            int offset = vertices * STRIDE + COLOR;
            unpackedData[offset] = ((alpha & 0xFF) << 24) |
                    ((blue & 0xFF) << 16) |
                    ((green & 0xFF) << 8) |
                    (red & 0xFF);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            int offset = vertices * STRIDE + UV0;
            unpackedData[offset] = Float.floatToRawIntBits(u);
            unpackedData[offset + 1] = Float.floatToRawIntBits(v);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            if (UV1 >= 0) {
                int offset = vertices * STRIDE + UV1;
                unpackedData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
                addElement();
            }
            return this;
        }

        @Override
        public VertexConsumer uv2(int u, int v) {
            int offset = vertices * STRIDE + UV2;
            unpackedData[offset] = (u & 0xFFFF) | ((v & 0xFFFF) << 16);
            addElement();
            return this;
        }

        @Override
        public VertexConsumer normal(float x, float y, float z) {
            int offset = vertices * STRIDE + NORMAL;
            unpackedData[offset] = ((int) (x * 127.0f) & 0xFF) |
                    (((int) (y * 127.0f) & 0xFF) << 8) |
                    (((int) (z * 127.0f) & 0xFF) << 16);
            addElement();
            return this;
        }

        @Override
        public void endVertex() {}

        @Override
        public void defaultColor(int defaultR, int defaultG, int defaultB, int defaultA) {}

        @Override
        public void unsetDefaultColor() {}

        public void put(int element, int... data) {
            for (int i = 0; i < 4; i++) {
                if (i < data.length) {
                    unpackedData[vertices * STRIDE + element] = data[i];
                } else {
                    unpackedData[vertices * STRIDE + element] = 0;
                }
            }
            addElement();
        }

        private void addElement() {
            elements++;
            if (elements == DefaultVertexFormat.BLOCK.getElements().size()) {
                vertices++;
                elements = 0;
            }
            if (vertices == 4) {
                full = true;
            }
        }

        public RecolorableBakedQuad build() {
            if (!full) {
                throw new IllegalStateException("not enough data");
            }
            if (texture == null) {
                throw new IllegalStateException("texture not set");
            }
            if (contractUVs) {
                float tX = texture.sprite().contents().width() / (texture.sprite().getU1() - texture.sprite().getU0());
                float tY = texture.sprite().contents().height() / (texture.sprite().getV1() - texture.sprite().getV0());
                float tS = Math.max(tX, tY);
                float ep = 1f / (tS * 0x100);
                float[] uvc = new float[4];
                for (int v = 0; v < 4; v++) {
                    for (int i = 0; i < 4; i++) {
                        uvc[i] += Float.intBitsToFloat(unpackedData[v * STRIDE + i] / 4);
                    }
                }
                for (int v = 0; v < 4; v++) {
                    for (int i = 0; i < 4; i++) {
                        float uo = Float.intBitsToFloat(unpackedData[v * STRIDE + i]);
                        float eps = 1f / 0x100;
                        float un = uo * (1 - eps) + uvc[i] * eps;
                        float ud = uo - un;
                        float aud = ud;
                        if (aud < 0) aud = -aud;
                        if (aud < ep) // not moving a fraction of a pixel
                        {
                            float udc = uo - uvc[i];
                            if (udc < 0) udc = -udc;
                            if (udc < 2 * ep) // center is closer than 2 fractions of a pixel, don't move too close
                            {
                                un = (uo + uvc[i]) / 2;
                            } else // move at least by a fraction
                            {
                                un = uo + (ud < 0 ? ep : -ep);
                            }
                        }
                        unpackedData[v * STRIDE + i] = Float.floatToRawIntBits(un);
                    }
                }
            }
            return new RecolorableBakedQuad(unpackedData, quadTint, quadOrientation, texture, shade,
                    hasAmbientOcclusion);
        }
    }

    private static int findOffset(VertexFormatElement element) {
        // Divide by 4 because we want the int offset
        var index = DefaultVertexFormat.BLOCK.getElements().indexOf(element);
        return index < 0 ? -1 : DefaultVertexFormat.BLOCK.getOffset(index) / 4;
    }
}
