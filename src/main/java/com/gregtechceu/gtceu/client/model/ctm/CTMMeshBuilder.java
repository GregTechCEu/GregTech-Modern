package com.gregtechceu.gtceu.client.model.ctm;

import com.gregtechceu.gtceu.client.model.quad.MeshBuilder;
import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.LinkedList;
import java.util.List;

import static com.gregtechceu.gtceu.client.model.quad.MutableQuadView.*;
import static com.gregtechceu.gtceu.client.util.ModelEventHelper.*;

public class CTMMeshBuilder {

    public static List<BakedQuad> buildCTMQuads(BlockAndTintGetter level, BlockPos pos, BlockState state,
                                                List<BakedQuad> quads, Direction cullFace) {
        TextureConnections connections = TextureConnections.getInstance();
        connections.fillSubmapCache(level, pos, state, cullFace);

        return buildCTMQuads(connections, quads, cullFace);
    }

    public static List<BakedQuad> buildCTMQuads(TextureConnections connections, List<BakedQuad> base,
                                                Direction cullFace) {
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

            for (int xQuadrant = 0; xQuadrant < 2; xQuadrant++) {
                for (int yQuadrant = 0; yQuadrant < 2; yQuadrant++) {
                    boolean defaultTexture = connections.isDefaultTexture(xQuadrant, yQuadrant);
                    TextureAtlasSprite ctmSprite = defaultTexture ? originalSprite : connectionSprite;

                    emitter.fromVanilla(originalQuad, cullFace);
                    emitter.spriteUnbake(originalSprite, BAKE_NORMALIZED | BAKE_DEROTATE_UV);
                    canonicalizeWinding(emitter);

                    // slice quad into the current quadrant
                    subsect(emitter, Submap.X2[xQuadrant][yQuadrant]);
                    remapUVs(emitter, connections.getSubmapFor(xQuadrant, yQuadrant));

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

    // these are only used within the below methods, but are stored here as consts to reduce allocations
    // because they can be reused infinitely. DO NOT USE OUTSIDE subsect()/transformUVs()!!

    // filled in first copyUv() calls
    private static final ThreadLocal<Vector2f[]> uvs = ThreadLocal.withInitial(() -> new Vector2f[4]);
    private static final ThreadLocal<Vector2f[]> uvExtremes = ThreadLocal.withInitial(() -> {
        return new Vector2f[] { new Vector2f(), new Vector2f() };
    });
    // set in copyPos() calls
    private static final ThreadLocal<Vector3f> position = ThreadLocal.withInitial(Vector3f::new);
    private static final ThreadLocal<Vector2f[]> xy = ThreadLocal.withInitial(() -> {
        return new Vector2f[] { new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f() };
    });
    private static final ThreadLocal<Vector2f[]> newXy = ThreadLocal.withInitial(() -> {
        return new Vector2f[] { new Vector2f(), new Vector2f(), new Vector2f(), new Vector2f() };
    });

    private static void remapUVs(MutableQuadView quad, ISubmap submap) {
        submap = submap.unitScale();

        Vector2f maxUV = CTMMeshBuilder.uvExtremes.get()[0];
        maxUV.set(Float.MIN_VALUE, Float.MIN_VALUE);

        // cache UVs
        Vector2f[] uvs = CTMMeshBuilder.uvs.get();
        for (int i = 0; i < 4; i++) {
            uvs[i] = quad.copyUv(i, uvs[i]);
            maxUV.max(uvs[i]);
        }
        // scale the quadrants' UVs to the quadrant's area
        scaleUVCoordinatesToQuadrant(uvs, maxUV);

        // recompute min & max UVs
        Vector2f[] uvExtremes = getUVExtremes(uvs);
        Vector2f minUV = uvExtremes[0];
        maxUV = uvExtremes[1];

        float width = maxUV.x - minUV.x;
        float height = maxUV.y - minUV.y;

        float minU = submap.getXOffset();
        float minV = submap.getYOffset();
        minU += minUV.x * submap.getWidth();
        minV += minUV.y * submap.getHeight();

        float maxU = minU + (width * submap.getWidth());
        float maxV = minV + (height * submap.getHeight());

        quad.uv(0, uvs[0].x <= minUV.x ? minU : maxU, uvs[0].y <= minUV.y ? minV : maxV);
        quad.uv(1, uvs[1].x <= minUV.x ? minU : maxU, uvs[1].y <= minUV.y ? minV : maxV);
        quad.uv(2, uvs[2].x <= minUV.x ? minU : maxU, uvs[2].y <= minUV.y ? minV : maxV);
        quad.uv(3, uvs[3].x <= minUV.x ? minU : maxU, uvs[3].y <= minUV.y ? minV : maxV);
    }

    private static void canonicalizeWinding(MutableQuadView emitter) {
        Direction face = emitter.nominalFace();
        if (face == null) return;

        int target = anchorIndex(emitter, face);
        if (target <= 0) return;

        Vector3f[] pos = new Vector3f[4];
        int[] color = new int[4];
        int[] light = new int[4];
        for (int i = 0; i < 4; i++) {
            pos[i] = emitter.copyPos(i, new Vector3f());
            color[i] = emitter.color(i);
            light[i] = emitter.lightmap(i);
        }
        for (int i = 0; i < 4; i++) {
            int s = (i + target) & 3;
            emitter.pos(i, pos[s].x, pos[s].y, pos[s].z);
            emitter.color(i, color[s]);
            emitter.lightmap(i, light[s]);
        }
    }

    private static int anchorIndex(MutableQuadView emitter, Direction face) {
        for (int i = 0; i < 4; i++) {
            float x = emitter.x(i), y = emitter.y(i), z = emitter.z(i);
            boolean hit = switch (face) {
                case UP -> near(x, 0.0F) && near(z, 0.0F);
                case DOWN -> near(x, 0.0F) && near(z, 1.0F);
                case NORTH -> near(x, 1.0F) && near(y, 1.0F);
                case SOUTH -> near(x, 0.0F) && near(y, 1.0F);
                case EAST -> near(y, 1.0F) && near(z, 1.0F);
                case WEST -> near(y, 1.0F) && near(z, 0.0F);
            };
            if (hit) return i;
        }
        return -1;
    }

    private static boolean near(float a, float b) {
        return Math.abs(a - b) < 0.01F;
    }

    // TODO simplify, this is quite long
    public static MutableQuadView subsect(final MutableQuadView quad, ISubmap submap) {
        Direction normal = quad.nominalFace();
        // nominalFace should never be null here; MutableQuadView.fromVanilla updates it
        assert normal != null;

        Vector2f[] xy = CTMMeshBuilder.xy.get();
        Vector2f[] newXy = CTMMeshBuilder.newXy.get();
        Vector3f position = CTMMeshBuilder.position.get();
        for (int i = 0; i < 4; i++) {
            // updates position
            quad.copyPos(i, position);

            switch (normal.getAxis()) {
                case X -> xy[i].set(position.z, position.y);
                case Y -> xy[i].set(position.x, position.z);
                case Z -> xy[i].set(position.x, position.y);
            }
        }

        if (normal != Direction.UP) {
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

        quad.uv(0, Mth.lerp(u0, quad.u(0), quad.u(3)), Mth.lerp(v0, quad.v(0), quad.v(1)));
        quad.uv(1, Mth.lerp(u1, quad.u(1), quad.u(2)), Mth.lerp(v1, quad.v(1), quad.v(0)));
        quad.uv(2, Mth.lerp(u2, quad.u(2), quad.u(1)), Mth.lerp(v2, quad.v(2), quad.v(3)));
        quad.uv(3, Mth.lerp(u3, quad.u(3), quad.u(0)), Mth.lerp(v3, quad.v(3), quad.v(2)));

        // spotless:off
        for (int i = 0; i < 4; i++) {
            switch (normal.getAxis()) {
                case X -> quad.pos(i, quad.x(i),  newXy[i].y, newXy[i].x);
                case Y -> quad.pos(i, newXy[i].x, quad.y(i),  newXy[i].y);
                case Z -> quad.pos(i, newXy[i].x, newXy[i].y, quad.z(i));
            }
        }
        // spotless:on

        return quad;
    }

    public static Vector2f[] getUVExtremes(Vector2f[] uvs) {
        Vector2f[] uvExtremes = CTMMeshBuilder.uvExtremes.get();
        uvExtremes[0].set(Float.MAX_VALUE, Float.MAX_VALUE);
        uvExtremes[1].set(Float.MIN_VALUE, Float.MIN_VALUE);

        for (int i = 0; i < 4; i++) {
            Vector2f vertexUV = uvs[i];
            uvExtremes[0].min(vertexUV);
            uvExtremes[1].max(vertexUV);
        }
        return uvExtremes;
    }

    private static void scaleUVCoordinatesToQuadrant(Vector2f[] uvs, Vector2f maxUV) {
        float minU = maxUV.x() - 0.5f > Mth.EPSILON ? 0.5f : 0.0f,
                minV = maxUV.y() - 0.5f > Mth.EPSILON ? 0.5f : 0.0f;
        float maxU = maxUV.x() - 0.5f > Mth.EPSILON ? 1.0f : 0.5f,
                maxV = maxUV.y() - 0.5f > Mth.EPSILON ? 1.0f : 0.5f;

        for (int i = 0; i < 4; i++) {
            // scale u,v to a 0-1 range
            uvs[i].set(normalize(uvs[i].x, minU, maxU), normalize(uvs[i].y, minV, maxV));
        }
    }

    /// scale {@code delta} to a 0-1 range based on {@code min} and {@code max}
    public static float normalize(float delta, float min, float max) {
        if (min == max) return 0.5f;
        return Mth.clamp(Mth.inverseLerp(delta, min, max), 0.0f, 1.0f);
    }
}
