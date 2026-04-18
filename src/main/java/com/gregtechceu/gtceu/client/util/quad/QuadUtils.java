package com.gregtechceu.gtceu.client.util.quad;

import com.gregtechceu.gtceu.client.model.ctm.CTMCache;
import com.gregtechceu.gtceu.client.model.ctm.ISubmap;
import com.gregtechceu.gtceu.client.model.quad.MeshBuilder;
import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;
import com.gregtechceu.gtceu.client.util.TextureHelper;
import com.gregtechceu.gtceu.client.util.quad.transformers.QuadReInterpolator;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IQuadTransformer;

import it.unimi.dsi.fastutil.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

import static com.gregtechceu.gtceu.client.model.quad.MutableQuadView.*;
import static com.gregtechceu.gtceu.client.util.ModelEventHelper.*;

public class QuadUtils {

    public static Pair<Vector2f, Vector2f> findMinMaxUVs(Vector2f[] uvs) {
        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE, maxU = Float.MIN_VALUE, maxV = Float.MIN_VALUE;

        for (int i = 0; i < 4; i++) {
            Vector2f uv = uvs[i];
            minU = Math.min(minU, uv.x());
            minV = Math.min(minV, uv.y());
            maxU = Math.max(maxU, uv.x());
            maxV = Math.max(maxV, uv.y());
        }
        return Pair.of(new Vector2f(minU, minV), new Vector2f(maxU, maxV));
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

    private static void putVertexData(int[] vertices, int index, Vector3f pos, Vector2f uv) {
        int posOffset = index * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
        vertices[posOffset] = Float.floatToRawIntBits(pos.x());
        vertices[posOffset + 1] = Float.floatToRawIntBits(pos.y());
        vertices[posOffset + 2] = Float.floatToRawIntBits(pos.z());

        int uvOffset = index * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
        vertices[uvOffset] = Float.floatToRawIntBits(uv.x());
        vertices[uvOffset + 1] = Float.floatToRawIntBits(uv.y());
    }

    public static Vector2f[] normalizeUVs(Vector2f min, Vector2f max, Vector2f... uvs) {
        Vector2f[] ret = new Vector2f[uvs.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = normalizeUV(min, max, uvs[i]);
        }
        return ret;
    }

    public static Vector2f normalizeUV(TextureAtlasSprite sprite, Vector2f vec) {
        return new Vector2f(
                Mth.inverseLerp(vec.x(), sprite.getU0(), sprite.getU1()),
                Mth.inverseLerp(vec.y(), sprite.getV0(), sprite.getV1()));
    }

    public static Vector2f normalizeUV(Vector2f min, Vector2f max, Vector2f vec) {
        return new Vector2f(
                Mth.inverseLerp(vec.x(), min.x(), max.x()),
                Mth.inverseLerp(vec.y(), min.y(), max.y()));
    }

    public static Vector2f[] relativizeUVs(TextureAtlasSprite sprite, Vector2f... uvs) {
        for (int i = 0; i < uvs.length; i++) {
            uvs[i] = relativizeUV(sprite, uvs[i]);
        }
        return uvs;
    }

    public static Vector2f relativizeUV(TextureAtlasSprite sprite, Vector2f vec) {
        return new Vector2f(
                Mth.lerp(vec.x(), sprite.getU0(), sprite.getU1()),
                Mth.lerp(vec.y(), sprite.getV0(), sprite.getV1()));
    }

    public static List<BakedQuad> buildCTMQuads(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                                List<BakedQuad> quads, @Nullable Direction cullFace) {
        CTMCache ctmCache = CTMCache.getInstance();
        if (cullFace != null) {
            ctmCache.getSubmapIds(level, pos, state, cullFace);
        }

        return buildCTMQuads(ctmCache, quads, cullFace);
    }

    public static List<BakedQuad> buildCTMQuads(CTMCache cachedConnections, List<BakedQuad> base,
                                                @Nullable Direction cullFace) {
        MeshBuilder meshBuilder = MeshBuilder.getInstance();
        var emitter = meshBuilder.getEmitter();

        QuadReInterpolator interpolator = new QuadReInterpolator();

        for (BakedQuad originalQuad : base) {
            TextureAtlasSprite sprite = originalQuad.getSprite();

            TextureAtlasSprite connection = CTM_SPRITE_CACHE.get(sprite.contents().name());
            if (connection == null) {
                emitter.fromVanilla(originalQuad, cullFace);
                emitter.emit();
                continue;
            }

            int[] ctm = cachedConnections.getSubmapIndices();

            for (int quadrant = 0; quadrant < 4; quadrant++) {
                TextureAtlasSprite ctmSprite = ctm[quadrant] > 15 ? originalQuad.getSprite() : connection;

                emitter.fromVanilla(originalQuad, cullFace);
                TextureHelper.normalizeBy(emitter, sprite);
                interpolator.setInputQuad(emitter);

                // slice quad into the current quadrant
                subsect(emitter, CTMCache.uvs[ctm[quadrant]].unitScale());

                interpolator.transform(emitter);

                // derotate quad here
                emitter.spriteBake(ctmSprite, BAKE_LOCK_UV | BAKE_NORMALIZED);

                emitter.emit();
            }
        }
        return meshBuilder.build().toBakedBlockQuads();
    }

    // TODO simplify, this is quite long
    public static MutableQuadView subsect(MutableQuadView quad, ISubmap submap) {
        Vector2f[] uvs = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            uvs[i] = quad.copyUv(i, uvs[i]);
        }
        int firstIndex = findMinUVIndex(uvs);

        Vector3f[] positions = new Vector3f[4];
        for (int i = 0; i < 4; i++) {
            int idx = (firstIndex + i) % 4;
            positions[i] = quad.copyPos(i, positions[idx]);
        }

        Direction normal = quad.nominalFace();

        Vector2f[] xy = new Vector2f[4];
        Vector2f[] newXy = new Vector2f[4];
        for (int i = 0; i < 4; i++) {
            switch (normal.getAxis()) {
                case Y -> xy[i] = new Vector2f(positions[i].x, positions[i].z);
                case Z -> xy[i] = new Vector2f(positions[i].x, positions[i].y);
                case X -> xy[i] = new Vector2f(positions[i].z, positions[i].y);
            }
            newXy[i] = new Vector2f();
        }

        if (normal.getAxis() != Direction.Axis.Y) {
            submap = submap.flipY();
        }
        if (normal == Direction.EAST || normal == Direction.NORTH) {
            submap = submap.flipX();
        }

        submap = submap.unitScale();

        if (normal.getAxis() == Direction.Axis.Y || normal.getAxisDirection() == Direction.AxisDirection.POSITIVE) {
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

        float u0 = normalize(xy[0].x, xy[3].x, newXy[0].x);
        float v0 = normalize(xy[0].y, xy[1].y, newXy[0].y);
        float u1 = normalize(xy[1].x, xy[2].x, newXy[1].x);
        float v1 = normalize(xy[1].y, xy[0].y, newXy[1].y);
        float u2 = normalize(xy[2].x, xy[1].x, newXy[2].x);
        float v2 = normalize(xy[2].y, xy[3].y, newXy[2].y);
        float u3 = normalize(xy[3].x, xy[0].x, newXy[3].x);
        float v3 = normalize(xy[3].y, xy[2].y, newXy[3].y);

        quad.uv(0, Mth.lerp(uvs[0].x, uvs[3].x, u0), Mth.lerp(uvs[0].y, uvs[1].y, v0));
        quad.uv(1, Mth.lerp(uvs[1].x, uvs[2].x, u1), Mth.lerp(uvs[1].y, uvs[0].y, v1));
        quad.uv(2, Mth.lerp(uvs[2].x, uvs[1].x, u2), Mth.lerp(uvs[2].y, uvs[3].y, v2));
        quad.uv(3, Mth.lerp(uvs[3].x, uvs[0].x, u3), Mth.lerp(uvs[3].y, uvs[2].y, v3));

        for (int i = 0; i < 4; i++) {
            switch (normal.getAxis()) {
                case Y -> quad.pos(i, newXy[i].x, quad.y(i), newXy[i].y);
                case Z -> quad.pos(i, newXy[i].x, newXy[i].y, quad.z(i));
                case X -> quad.pos(i, quad.x(i), newXy[i].y, newXy[i].x);
            }
        }

        return quad;
    }

    public static float normalize(float min, float max, float x) {
        if (min == max) return 0.5f;
        return Mth.inverseLerp(x, min, max);
    }
}
