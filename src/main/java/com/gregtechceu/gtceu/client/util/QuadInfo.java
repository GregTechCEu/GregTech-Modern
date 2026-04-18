package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.client.model.ctm.ISubmap;
import com.gregtechceu.gtceu.client.model.ctm.OctagonalOrientation;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.IQuadTransformer;

import it.unimi.dsi.fastutil.Pair;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static com.gregtechceu.gtceu.client.model.ctm.OctagonalOrientation.*;

public record QuadInfo(TextureAtlasSprite sprite, int tintIndex, Direction direction,
                       boolean shade, boolean ao, int blockLight, int skyLight,
                       Vector3f[] vertices, Vector2f[] uvs, Vector2f minUV, Vector2f maxUV) {

    // Mapping the different corner indices to their respective dirs
    private static final OctagonalOrientation[][] SUBMAP_MAP = new OctagonalOrientation[][] {
            { BOTTOM, LEFT, BOTTOM_LEFT },
            { BOTTOM, RIGHT, BOTTOM_RIGHT },
            { TOP, RIGHT, TOP_RIGHT },
            { TOP, LEFT, TOP_LEFT }
    };

    public QuadInfo(TextureAtlasSprite sprite, int tintIndex, Direction direction, boolean shade, boolean ao,
                    Vector3f[] vertices, Vector2f[] uvs, Vector2f minUV, Vector2f maxUV) {
        this(sprite, tintIndex, direction, shade, ao, 0, 0, vertices, uvs, minUV, maxUV);
    }

    // region UV operations

    public Vector2f[] normalizeUVs() {
        Vector2f[] ret = new Vector2f[uvs.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = QuadUtils.normalizeUV(sprite, uvs[i]);
        }
        return ret;
    }

    public Vector2f[] relativizeUVs() {
        Vector2f[] ret = new Vector2f[uvs.length];
        for (int i = 0; i < uvs.length; i++) {
            ret[i] = QuadUtils.relativizeUV(sprite, uvs[i]);
        }
        return ret;
    }

    public int getNormalizedUVQuadrant() {
        return getUVQuadrant(QuadUtils.normalizeUV(sprite, maxUV));
    }

    public static int getUVQuadrant(Vector2f uv) {
        if (uv.x() <= 0.5f) {
            if (uv.y() <= 0.5f) {
                return 3;
            } else {
                return 0;
            }
        } else {
            if (uv.y() <= 0.5f) {
                return 2;
            } else {
                return 1;
            }
        }
    }

    public Vector2f[] normalizeUVQuadrant() {
        Vector2f[] normalized = normalizeUVs();

        int quadrant = getNormalizedUVQuadrant();
        float minUInterp = quadrant == 1 || quadrant == 2 ? 0.5f : 0;
        float minVInterp = quadrant < 2 ? 0.5f : 0;
        float maxUInterp = quadrant == 0 || quadrant == 3 ? 0.5f : 1;
        float maxVInterp = quadrant > 1 ? 0.5f : 1;

        normalized = QuadUtils.normalizeUVs(
                new Vector2f(minUInterp, minVInterp),
                new Vector2f(maxUInterp, maxVInterp),
                normalized);
        return QuadUtils.relativizeUVs(sprite, normalized);
    }

    public Vector2f[] transformUVData(TextureAtlasSprite other, ISubmap submap) {
        Vector2f[] normalized = normalizeUVs();
        var maxUVs = QuadUtils.findMinMaxUVs(normalized);
        submap = submap.unitScale();

        float width = maxUVs.second().x() - maxUVs.first().x();
        float height = maxUVs.second().y() - maxUVs.first().y();

        float minU = submap.getXOffset();
        float minV = submap.getYOffset();
        minU += maxUVs.first().x() * submap.getWidth();
        minV += maxUVs.first().y() * submap.getHeight();

        float maxU = minU + (width * submap.getWidth());
        float maxV = minV + (height * submap.getHeight());

        Vector2f[] newUVs = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            Vector2f uv = new Vector2f(this.uvs[i]);
            // same as sprite.getU(oldSprite.getUOffset(uv.x)), but we don't multiply and divide in between
            // NOTE: can be simplified to ^ on 1.21
            uv.x = Mth.map(uv.x, this.sprite.getU0(), this.sprite.getU1(), other.getU0(), other.getU1());
            uv.y = Mth.map(uv.y, this.sprite.getV0(), this.sprite.getV1(), other.getV0(), other.getV1());
            newUVs[i] = uv;
        }

        // FIXME this... isn't all that great.
        newUVs = new Vector2f[] {
                new Vector2f(newUVs[0].x() == this.minUV.x() ? minU : maxU, newUVs[0].y() == this.minUV.y() ? minV : maxV),
                new Vector2f(newUVs[1].x() == this.minUV.x() ? minU : maxU, newUVs[1].y() == this.minUV.y() ? minV : maxV),
                new Vector2f(newUVs[2].x() == this.minUV.x() ? minU : maxU, newUVs[2].y() == this.minUV.y() ? minV : maxV),
                new Vector2f(newUVs[3].x() == this.minUV.x() ? minU : maxU, newUVs[3].y() == this.minUV.y() ? minV : maxV)
        };
        return QuadUtils.relativizeUVs(other, newUVs);
    }

    public Vector2f[] copyUVs() {
        Vector2f[] result = new Vector2f[uvs.length];
        for (int i = 0; i < uvs.length; ++i) {
            result[i] = new Vector2f(uvs[i]);
        }
        return result;
    }

    // endregion

    // region vertex operations

    public Vector3f[] copyVertices() {
        Vector3f[] result = new Vector3f[vertices.length];
        for (int i = 0; i < vertices.length; ++i) {
            result[i] = new Vector3f(vertices[i]);
        }
        return result;
    }

    public QuadInfo grow() {
        return new QuadInfo(sprite, tintIndex, direction, shade, ao, blockLight, skyLight,
                copyVertices(), normalizeUVQuadrant(), new Vector2f(minUV), new Vector2f(maxUV));
    }

    public QuadInfo copy() {
        return new QuadInfo(sprite, tintIndex, direction, shade, ao, blockLight, skyLight,
                copyVertices(), copyUVs(), new Vector2f(minUV), new Vector2f(maxUV));
    }

    public QuadInfo transformUVs(TextureAtlasSprite sprite, ISubmap submap) {
        return new QuadInfo(sprite, tintIndex, direction, shade, ao, blockLight, skyLight,
                copyVertices(), transformUVData(sprite, submap), new Vector2f(minUV), new Vector2f(maxUV));
    }

    public QuadInfo[] subdivide() {
        QuadInfo[] rects = new QuadInfo[4];

        var firstDivide = this.divide(false);
        var secondDivide = firstDivide.left().divide(true);
        rects[0] = secondDivide.left();
        if (firstDivide.right() != null) {
            Pair<QuadInfo, QuadInfo> thirdDivide = firstDivide.right().divide(true);
            rects[1] = thirdDivide.left();
            rects[2] = thirdDivide.right();
        }
        rects[3] = secondDivide.right();

        return rects;
    }

    private Pair<QuadInfo, QuadInfo> divide(boolean vertical) {
        Vector2f[] normalizedUvs = this.normalizeUVs();
        float minUV, maxUV;
        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE, maxU = Float.MIN_VALUE, maxV = Float.MIN_VALUE;

        int start = 0;
        for (int i = 0; i < 4; i++) {
            if (minU >= normalizedUvs[i].x() && minV >= normalizedUvs[i].y()) {
                start = i;
            }
            minU = Math.min(minU, normalizedUvs[i].x());
            minV = Math.min(minV, normalizedUvs[i].y());
            maxU = Math.max(maxU, normalizedUvs[i].x());
            maxV = Math.max(maxV, normalizedUvs[i].y());
        }
        if (vertical) {
            minUV = minV;
            maxUV = maxV;
        } else {
            minUV = minU;
            maxUV = maxU;
        }

        if (minUV < 0.5f && maxUV > 0.5f) {
            float delta = Mth.inverseLerp(minUV, maxUV, 0.5f);

            Vector2f[] firstUVs = QuadUtils.relativizeUVs(sprite,
                    new Vector2f(vertical ? minU : 0.5f, vertical ? 0.5f : minV),
                    new Vector2f(vertical ? minU : 0.5f, maxV),
                    new Vector2f(maxU, maxV),
                    new Vector2f(maxU, vertical ? 0.5f : minV));
            Vector2f[] secondUVs = QuadUtils.relativizeUVs(sprite,
                    new Vector2f(minU, minV),
                    new Vector2f(minU, vertical ? 0.5f : maxV),
                    new Vector2f(vertical ? maxU : 0.5f, vertical ? 0.5f : maxV),
                    new Vector2f(vertical ? maxU : 0.5f, minV));

            Vector3f[] firstVerts = new Vector3f[4];
            Vector3f[] secondVerts = new Vector3f[4];
            for (int i = 0; i < 4; i++) {
                int idx = (start + i) % 4;
                firstVerts[i] = new Vector3f(vertices[idx]);
                secondVerts[i] = new Vector3f(vertices[idx]);
            }

            int i0 = 0;
            int i1 = vertical ? 1 : 3;
            int i2 = 2;
            int i3 = vertical ? 3 : 1;

            firstVerts[i0].lerp(firstVerts[i1], delta);
            firstVerts[i3].lerp(firstVerts[i2], delta);
            secondVerts[i0].lerp(secondVerts[i1], delta, secondVerts[i1]);
            secondVerts[i3].lerp(secondVerts[i2], delta, secondVerts[i2]);

            return Pair.of(
                    new QuadInfo(sprite, tintIndex, direction, shade, ao, blockLight, skyLight,
                            firstVerts, firstUVs, firstUVs[0], firstUVs[2]),
                    new QuadInfo(sprite, tintIndex, direction, shade, ao, blockLight, skyLight,
                            secondVerts, secondUVs, secondUVs[0], secondUVs[2]));
        } else {
            return Pair.of(this, null);
        }
    }

    // endregion

    public BakedQuad rebake() {
        int[] vertexData = new int[32];
        for (int i = 0; i < 4; ++i) {
            Vector3f pos = vertices[i];
            Vector2f uv = uvs[i];
            Vector2f nextUv = uvs[(i + 2) % 4];

            int v = i * IQuadTransformer.STRIDE;
            vertexData[v + IQuadTransformer.POSITION] = Float.floatToRawIntBits(pos.x());
            vertexData[v + IQuadTransformer.POSITION + 1] = Float.floatToRawIntBits(pos.y());
            vertexData[v + IQuadTransformer.POSITION + 2] = Float.floatToRawIntBits(pos.z());
            vertexData[v + IQuadTransformer.COLOR] = 0xffffffff;
            vertexData[v + IQuadTransformer.UV0] = Float.floatToRawIntBits(
                    sprite.getU(uv.x() * 0.999f + nextUv.x() * 0.001f));
            vertexData[v + IQuadTransformer.UV0 + 1] = Float.floatToRawIntBits(
                    sprite.getV(uv.y() * 0.999f + nextUv.y() * 0.001f));
            vertexData[i * IQuadTransformer.STRIDE + IQuadTransformer.UV2] = LightTexture.pack(blockLight, skyLight);
        }
        return new BakedQuad(vertexData, tintIndex, direction, sprite, shade, ao);
    }
}
