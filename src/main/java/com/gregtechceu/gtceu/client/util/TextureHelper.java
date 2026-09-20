/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static com.gregtechceu.gtceu.client.model.quad.MutableQuadView.*;

/**
 * Handles most texture-baking use cases for model loaders and model libraries via
 * {@link #bakeSprite(MutableQuadView, TextureAtlasSprite, int)}.
 */
@UtilityClass
public class TextureHelper {

    private static final int BAKE_ROTATE_ANY = BAKE_ROTATE_270 | BAKE_ROTATE_180 | BAKE_ROTATE_90;

    /**
     * Bakes textures in the provided vertex data, handling UV locking, rotation, interpolation, etc.
     * Textures must not be already baked.
     *
     * <p>
     * If {@code sprite == null}, only the UV modifiers will be applied,
     * but they won't be translated to the sprite's atlas coordinates.
     *
     * @see #unbakeSprite(MutableQuadView, TextureAtlasSprite, int)
     * @see MutableQuadView#BAKE_ROTATE_NONE bake flags
     */
    public static void bakeSprite(MutableQuadView quad, @Nullable TextureAtlasSprite sprite, int bakeFlags) {
        if (quad.nominalFace() != null && (BAKE_LOCK_UV & bakeFlags) != 0) {
            // Assigns normalized UV coordinates based on vertex positions
            applyModifier(quad, UV_LOCKERS[quad.nominalFace().get3DDataValue()]);
        }

        final int rotation = bakeFlags & BAKE_ROTATE_ANY;

        if (rotation != 0) {
            // Rotates texture around the center of sprite.
            // Assumes normalized coordinates.
            applyModifier(quad, ROTATIONS[rotation]);
        }

        if ((BAKE_FLIP_U & bakeFlags) != 0) {
            // Inverts U coordinates. Assumes normalized (0-1) values.
            applyModifier(quad, (q, i) -> q.uv(i, 1 - q.u(i), q.v(i)));
        }

        if ((BAKE_FLIP_V & bakeFlags) != 0) {
            // Inverts V coordinates. Assumes normalized (0-1) values.
            applyModifier(quad, (q, i) -> q.uv(i, q.u(i), 1 - q.v(i)));
        }

        if (sprite != null) {
            interpolate(quad, sprite);
        }
    }

    /**
     * Faster than sprite method. Sprite computes span and normalizes inputs each call, so we'd have to denormalize
     * before we called, only to have the sprite renormalize immediately.
     */
    public static void interpolate(MutableQuadView q, TextureAtlasSprite sprite) {
        final float uMin = sprite.getU0();
        final float uSpan = sprite.getU1() - uMin;
        final float vMin = sprite.getV0();
        final float vSpan = sprite.getV1() - vMin;

        for (int i = 0; i < 4; i++) {
            q.uv(i, uMin + q.u(i) * uSpan, vMin + q.v(i) * vSpan);
        }
    }

    /**
     * The reverse operation of {@link #bakeSprite}. Undoes the same operations <i>except UV locking</i>.
     * Textures must be already baked.
     *
     * <p>
     * Note this the function's order of operations is reversed in relation to {@link #bakeSprite}.
     *
     * <p>
     * If {@code sprite == null}, only the UV modifiers will be applied,
     * but they won't be translated from the sprite's atlas coordinates to a 0-16 range.
     *
     * @see #bakeSprite(MutableQuadView, TextureAtlasSprite, int)
     * @see MutableQuadView#BAKE_ROTATE_NONE bake flags
     */
    public static void unbakeSprite(MutableQuadView quad, @Nullable TextureAtlasSprite sprite, int bakeFlags) {
        if (sprite != null) {
            deInterpolate(quad, sprite);
        }

        if ((BAKE_FLIP_V & bakeFlags) != 0) {
            // Inverts V coordinates. Assumes normalized (0-1) values.
            applyModifier(quad, (q, i) -> q.uv(i, q.u(i), 1 - q.v(i)));
        }

        if ((BAKE_FLIP_U & bakeFlags) != 0) {
            // Inverts U coordinates. Assumes normalized (0-1) values.
            applyModifier(quad, (q, i) -> q.uv(i, 1 - q.u(i), q.v(i)));
        }

        final int rotation = bakeFlags & BAKE_ROTATE_ANY;

        if (rotation != 0) {
            // Rotates texture around the center of sprite.
            // Assumes normalized coordinates.
            applyModifier(quad, ROTATIONS[rotation]);
        }

        if ((UNBAKE_DEROTATE_UV & bakeFlags) != 0) {
            // Cycles texture coordinates so that vertex 0's UVs are the smallest
            derotateUV(quad);
        }
        if ((UNBAKE_CANONICALIZE_WINDING & bakeFlags) != 0) {
            // Cycles texture coordinates so that vertex 0's UVs are the smallest
            canonicalizeWinding(quad);
        }
    }

    /**
     * Faster than sprite method. Sprite computes span and normalizes inputs each call, so we'd have to denormalize
     * before we called, only to have the sprite renormalize immediately.
     */
    public static void deInterpolate(MutableQuadView q, TextureAtlasSprite sprite) {
        final float uMin = sprite.getU0();
        final float uSpan = sprite.getU1() - uMin;
        final float vMin = sprite.getV0();
        final float vSpan = sprite.getV1() - vMin;

        for (int i = 0; i < 4; i++) {
            q.uv(i, (q.u(i) - uMin) / uSpan, (q.v(i) - vMin) / vSpan);
        }
    }

    private static final ThreadLocal<Vector3f[]> positions = ThreadLocal.withInitial(() -> new Vector3f[4]);
    private static final ThreadLocal<int[]> color = ThreadLocal.withInitial(() -> new int[4]);
    private static final ThreadLocal<int[]> light = ThreadLocal.withInitial(() -> new int[4]);

    private static void derotateUV(MutableQuadView quad) {
        int minIndex = 0;
        float minU = Float.MAX_VALUE, minV = Float.MAX_VALUE;

        for (int i = 0; i < 4; i++) {
            if (quad.u(i) <= minU && quad.v(i) <= minV) {
                minIndex = i;
                minU = quad.u(i);
                minV = quad.v(i);
            }
        }
        applyModifier(quad, ROTATIONS[minIndex]);
    }

    private static void canonicalizeWinding(MutableQuadView quad) {
        Direction face = quad.nominalFace();
        if (face == null) return;

        int target = anchorIndex(quad, face);
        // no need to do anything if target is 0 as that means the winding is already correct
        if (target <= 0) return;

        Vector3f[] pos = TextureHelper.positions.get();
        int[] color = TextureHelper.color.get();
        int[] light = TextureHelper.light.get();
        for (int i = 0; i < 4; i++) {
            pos[i] = quad.copyPos(i, pos[i]);
            color[i] = quad.color(i);
            light[i] = quad.lightmap(i);
        }
        for (int i = 0; i < 4; i++) {
            int idx = (i + target) & 3;
            quad.pos(i, pos[idx]);
            quad.color(i, color[idx]);
            quad.lightmap(i, light[idx]);
        }
    }

    private static int anchorIndex(MutableQuadView quad, Direction face) {
        for (int i = 0; i < 4; i++) {
            boolean hit = switch (face) {
                case UP -> Mth.equal(quad.x(i), 0.0F) && Mth.equal(quad.z(i), 0.0F);
                case DOWN -> Mth.equal(quad.x(i), 0.0F) && Mth.equal(quad.z(i), 1.0F);
                case NORTH -> Mth.equal(quad.x(i), 1.0F) && Mth.equal(quad.y(i), 1.0F);
                case SOUTH -> Mth.equal(quad.x(i), 0.0F) && Mth.equal(quad.y(i), 1.0F);
                case EAST -> Mth.equal(quad.y(i), 1.0F) && Mth.equal(quad.z(i), 1.0F);
                case WEST -> Mth.equal(quad.y(i), 1.0F) && Mth.equal(quad.z(i), 0.0F);
            };
            if (hit) return i;
        }
        return -1;
    }

    @FunctionalInterface
    public interface VertexModifier {

        void apply(MutableQuadView quad, int vertexIndex);
    }

    private static void applyModifier(MutableQuadView quad, VertexModifier modifier) {
        for (int i = 0; i < 4; i++) {
            modifier.apply(quad, i);
        }
    }

    private static final VertexModifier[] ROTATIONS = new VertexModifier[] {
            (q, i) -> {}, // 0
            (q, i) -> q.uv(i, q.v(i), 1 - q.u(i)), // 90
            (q, i) -> q.uv(i, 1 - q.u(i), 1 - q.v(i)), // 180
            (q, i) -> q.uv(i, 1 - q.v(i), q.u(i)) // 270
    };

    private static final VertexModifier[] UV_LOCKERS = new VertexModifier[6];

    static {
        UV_LOCKERS[Direction.EAST.get3DDataValue()] = (q, i) -> q.uv(i, 1 - q.z(i), 1 - q.y(i));
        UV_LOCKERS[Direction.WEST.get3DDataValue()] = (q, i) -> q.uv(i, q.z(i), 1 - q.y(i));
        UV_LOCKERS[Direction.NORTH.get3DDataValue()] = (q, i) -> q.uv(i, 1 - q.x(i), 1 - q.y(i));
        UV_LOCKERS[Direction.SOUTH.get3DDataValue()] = (q, i) -> q.uv(i, q.x(i), 1 - q.y(i));
        UV_LOCKERS[Direction.DOWN.get3DDataValue()] = (q, i) -> q.uv(i, q.x(i), 1 - q.z(i));
        UV_LOCKERS[Direction.UP.get3DDataValue()] = (q, i) -> q.uv(i, q.x(i), q.z(i));
    }
}
