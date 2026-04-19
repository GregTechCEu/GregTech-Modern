package com.gregtechceu.gtceu.client.util.quad;

import com.gregtechceu.gtceu.client.model.ctm.CTMCache;
import com.gregtechceu.gtceu.client.model.ctm.ISubmap;
import com.gregtechceu.gtceu.client.model.ctm.Submap;
import com.gregtechceu.gtceu.client.model.quad.MeshBuilder;
import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;
import com.gregtechceu.gtceu.client.util.TextureHelper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector2ic;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;

import static com.gregtechceu.gtceu.client.model.quad.MutableQuadView.*;
import static com.gregtechceu.gtceu.client.util.ModelEventHelper.*;

public class QuadUtils {

    public static Vector2f[] findMinMaxUVs(Vector2f[] uvs) {
        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE, maxU = Float.MIN_VALUE, maxV = Float.MIN_VALUE;

        for (int i = 0; i < 4; i++) {
            Vector2f uv = uvs[i];
            minU = Math.min(minU, uv.x());
            minV = Math.min(minV, uv.y());
            maxU = Math.max(maxU, uv.x());
            maxV = Math.max(maxV, uv.y());
        }
        return new Vector2f[]{ new Vector2f(minU, minV), new Vector2f(maxU, maxV) };
    }

    public static int findMinUVIndex(Vector2f[] uvs) {
        int minIndex = 0;
        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE;

        for (int v = 0; v < 4; v++) {
            Vector2f uv = uvs[v];
            if (uv.x() <= minU && uv.y() <= minV) {
                minIndex = v;
                minU = uv.x();
                minV = uv.y();
            }
        }
        return minIndex;
    }

    public static List<BakedQuad> buildCTMQuads(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                                List<BakedQuad> quads, @Nullable Direction cullFace) {
        CTMCache ctmCache = CTMCache.getInstance();
        if (cullFace != null) {
            ctmCache.fillSubmapCache(level, pos, state, cullFace);
        }

        return buildCTMQuads(ctmCache, quads, cullFace);
    }

    public static List<BakedQuad> buildCTMQuads(CTMCache cachedConnections, List<BakedQuad> base,
                                                @Nullable Direction cullFace) {
        List<BakedQuad> result = new LinkedList<>();
        MeshBuilder meshBuilder = MeshBuilder.getInstance();
        var emitter = meshBuilder.getEmitter();

        for (BakedQuad originalQuad : base) {
            TextureAtlasSprite originalSprite = originalQuad.getSprite();

            TextureAtlasSprite connectionSprite = CTM_SPRITE_CACHE.get(originalSprite.contents().name());
            if (connectionSprite == null) {
                result.add(originalQuad);
                continue;
            }

            Vector2ic[][] ctm = cachedConnections.getCachedSubmapIndices();

            for (int xQ = 0; xQ < 2; xQ++) {
                for (int yQ = 0; yQ < 2; yQ++) {
                    boolean defaultTexture = CTMCache.isDefaultTexture(ctm[xQ][yQ]);
                    TextureAtlasSprite ctmSprite = defaultTexture ? originalSprite : connectionSprite;

                    emitter.fromVanilla(originalQuad, cullFace);
                    TextureHelper.unbakeSprite(emitter, originalSprite, BAKE_NORMALIZED);

                    // slice quad into the current quadrant
                    subsect(emitter, Submap.X2[yQ][xQ]);
                    transformUVs(emitter, CTMCache.getSubmapFor(ctm[xQ][yQ]));

                    emitter.spriteBake(ctmSprite, BAKE_NORMALIZED);

                    emitter.computeGeometry();
                    emitter.populateMissingNormals();

                    result.add(emitter.toBakedQuad(ctmSprite));
                    emitter.emit();
                }
            }
        }
        return result;
    }

    /**
     * See {@link #subsect(MutableQuadView, ISubmap)}'s documentation for the quadrant layout table.
     * @see #subsect(MutableQuadView, ISubmap)
     */
    private static void growQuadrantUVs(Vector2f[] uvs, Vector2f maxUV) {
        float minUInterp = maxUV.x > 0.5f ? 0.5f : 0.0f,
                minVInterp = maxUV.y > 0.5f ? 0.5f : 0.0f;
        float maxUInterp = maxUV.x > 0.5f ? 1.0f : 0.5f,
                maxVInterp = maxUV.y > 0.5f ? 1.0f : 0.5f;

        normalize(minUInterp, minVInterp, maxUInterp, maxVInterp, uvs[0]);
        normalize(minUInterp, minVInterp, maxUInterp, maxVInterp, uvs[1]);
        normalize(minUInterp, minVInterp, maxUInterp, maxVInterp, uvs[2]);
        normalize(minUInterp, minVInterp, maxUInterp, maxVInterp, uvs[3]);
    }

    // these are only used within the below methods, but are stored here as consts to reduce allocations
    // because they can be reused infinitely. DO NOT USE OUTSIDE subsect()/transformUVs()!!

    // filled in first copyUv() calls
    private static final ThreadLocal<Vector2f[]> uvs = ThreadLocal.withInitial(() -> new Vector2f[4]);
    // set in copyPos() calls
    private static final ThreadLocal<Vector3f> position = ThreadLocal.withInitial(Vector3f::new);
    private static final ThreadLocal<Vector2f[]> xy = ThreadLocal.withInitial(() -> {
        return new Vector2f[] { new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f() };
    });
    private static final ThreadLocal<Vector2f[]> newXy = ThreadLocal.withInitial(() -> {
        return new Vector2f[] { new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f() };
    });

    private static void transformUVs(MutableQuadView quad, ISubmap submap) {
        submap = submap.unitScale();

        Vector2f[] uvs = QuadUtils.uvs.get();
        for (int i = 0; i < 4; i++) {
            uvs[i] = quad.copyUv(i, uvs[i]);
        }
        Vector2f[] minMaxUVs = findMinMaxUVs(uvs);
        growQuadrantUVs(uvs, minMaxUVs[1]);

        // recompute min & max UVs
        minMaxUVs = findMinMaxUVs(uvs);
        Vector2f minUV = minMaxUVs[0], maxUV = minMaxUVs[1];

        float width = maxUV.x - minUV.x;
        float height = maxUV.y - minUV.y;

        float minU = submap.getXOffset();
        float minV = submap.getYOffset();
        minU += minUV.x * submap.getWidth();
        minV += minUV.y * submap.getHeight();

        float maxU = minU + (width * submap.getWidth());
        float maxV = minV + (height * submap.getHeight());

        quad.uv(0, uvs[0].x == minUV.x ? minU : maxU, uvs[0].y == minUV.y ? minV : maxV);
        quad.uv(1, uvs[1].x == minUV.x ? minU : maxU, uvs[1].y == minUV.y ? minV : maxV);
        quad.uv(2, uvs[2].x == minUV.x ? minU : maxU, uvs[2].y == minUV.y ? minV : maxV);
        quad.uv(3, uvs[3].x == minUV.x ? minU : maxU, uvs[3].y == minUV.y ? minV : maxV);
    }

    /**
     * Quadrant table is as follows:
     * <pre>
     * ╔══════╤══════╗
     * ║      │      ║
     * ║  2   │  3   ║
     * ╟──────┼──────╢
     * ║      │      ║
     * ║  0   │  1   ║
     * ╚══════╧══════╝
     * </pre>
     */
    // TODO simplify, this is quite long
    public static MutableQuadView subsect(MutableQuadView quad, ISubmap submap) {
        Direction normal = quad.nominalFace();

        Vector2f[] uvs = QuadUtils.uvs.get();
        for (int i = 0; i < 4; i++) {
            uvs[i] = quad.copyUv(i, uvs[i]);
        }
        int firstIndex = findMinUVIndex(uvs);

        Vector2f[] xy = QuadUtils.xy.get();
        Vector2f[] newXy = QuadUtils.newXy.get();
        Vector3f position = QuadUtils.position.get();
        for (int i = 0; i < 4; i++) {
            int idx = (firstIndex + i) % 4;
            // updates position
            quad.copyPos(idx, position);

            switch (normal.getAxis()) {
                case X -> xy[i].set(position.z, position.y);
                case Y -> xy[i].set(position.x, position.z);
                case Z -> xy[i].set(position.x, position.y);
            }
        }

        if (normal.getAxis() != Direction.Axis.Y) {
            submap = submap.flipY();
        }
        if (normal == Direction.EAST || normal == Direction.NORTH) {
            submap = submap.flipX();
        }

        submap = submap.unitScale();

        if (normal.getAxis() == Direction.Axis.Y || normal == Direction.SOUTH || normal == Direction.WEST) {
            // Relative X is the same sign for DOWN, UP, SOUTH, and WEST
            newXy[0].x = Math.max(xy[0].x, submap.getXOffset());                      // DUSW
            newXy[1].x = Math.max(xy[1].x, submap.getXOffset());                      // DUSW
            newXy[2].x = Math.min(xy[2].x, submap.getXOffset() + submap.getWidth());  // DUSW
            newXy[3].x = Math.min(xy[3].x, submap.getXOffset() + submap.getWidth());  // DUSW
        } else {
            // Flip relative X for NORTH and EAST
            newXy[0].x = Math.min(xy[0].x, submap.getXOffset() + submap.getWidth());  // NE
            newXy[1].x = Math.min(xy[1].x, submap.getXOffset() + submap.getWidth());  // NE
            newXy[2].x = Math.max(xy[2].x, submap.getXOffset());                      // NE
            newXy[3].x = Math.max(xy[3].x, submap.getXOffset());                      // NE
        }
        if (normal != Direction.UP) {
            // Relative Y is the same sign for all but UP
            newXy[0].y = Math.min(xy[0].y, submap.getYOffset() + submap.getHeight()); // DNSWE
            newXy[1].y = Math.max(xy[1].y, submap.getYOffset());                      // DNSWE
            newXy[2].y = Math.max(xy[2].y, submap.getYOffset());                      // DNSWE
            newXy[3].y = Math.min(xy[3].y, submap.getYOffset() + submap.getHeight()); // DNSWE
        } else {
            // Flip relative Y for UP
            newXy[0].y = Math.max(xy[0].y, submap.getYOffset());                      // U
            newXy[1].y = Math.min(xy[1].y, submap.getYOffset() + submap.getHeight()); // U
            newXy[2].y = Math.min(xy[2].y, submap.getYOffset() + submap.getHeight()); // U
            newXy[3].y = Math.max(xy[3].y, submap.getYOffset());                      // U
        }

        float u0 = normalize(newXy[0].x, xy[0].x, xy[3].x),
                v0 = normalize(newXy[0].y, xy[0].y, xy[1].y);
        float u1 = normalize(newXy[1].x, xy[1].x, xy[2].x),
                v1 = normalize(newXy[1].y, xy[1].y, xy[0].y);
        float u2 = normalize(newXy[2].x, xy[2].x, xy[1].x),
                v2 = normalize(newXy[2].y, xy[2].y, xy[3].y);
        float u3 = normalize(newXy[3].x, xy[3].x, xy[0].x),
                v3 = normalize(newXy[3].y, xy[3].y, xy[2].y);

        quad.uv(0, Mth.lerp(u0, uvs[0].x, uvs[3].x), Mth.lerp(v0, uvs[0].y, uvs[1].y));
        quad.uv(1, Mth.lerp(u1, uvs[1].x, uvs[2].x), Mth.lerp(v1, uvs[1].y, uvs[0].y));
        quad.uv(2, Mth.lerp(u2, uvs[2].x, uvs[1].x), Mth.lerp(v2, uvs[2].y, uvs[3].y));
        quad.uv(3, Mth.lerp(u3, uvs[3].x, uvs[0].x), Mth.lerp(v3, uvs[3].y, uvs[2].y));

        for (int i = 0; i < 4; i++) {
            switch (normal.getAxis()) {
                case X -> quad.pos(i, quad.x(i), newXy[i].y, newXy[i].x);
                case Y -> quad.pos(i, newXy[i].x, quad.y(i), newXy[i].y);
                case Z -> quad.pos(i, newXy[i].x, newXy[i].y, quad.z(i));
            }
        }

        return quad;
    }

    /// scale {@code value} to a 0-1 range component-wise based on {@code min} and {@code max}
    private static Vector2f normalize(float minU, float minV, float maxU, float maxV, Vector2f value) {
        value.set(normalize(value.x, minU, maxU), normalize(value.y, minV, maxV));
        return value;
    }

    /// scale {@code delta} to a 0-1 range based on {@code min} and {@code max}
    public static float normalize(float delta, float min, float max) {
        if (min == max) return 0.5f;
        return Mth.inverseLerp(delta, min, max);
    }
}
