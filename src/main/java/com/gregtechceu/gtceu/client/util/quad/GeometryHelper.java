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
package com.gregtechceu.gtceu.client.util.quad;

import com.gregtechceu.gtceu.client.model.quad.QuadView;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

/**
 * Static routines of general utility for renderer implementations.
 * Renderers are not required to use these helpers, but they were designed to be usable without the default renderer.
 */
@UtilityClass
public class GeometryHelper {

    /** Result from {@link #toFaceIndex(Direction)} for null values. */
    public static final int NULL_FACE_ID = 6;

    /**
     * Convenient way to encode faces that may be null. Null is returned as {@link #NULL_FACE_ID}.
     * Use {@link #faceFromIndex(int)} to retrieve encoded face.
     */
    public static int toFaceIndex(@Nullable Direction face) {
        return face == null ? NULL_FACE_ID : face.get3DDataValue();
    }

    /**
     * Use to decode a result from {@link #toFaceIndex(Direction)}. Return value will be null if encoded value was null.
     */
    @Contract("null -> null")
    public static @Nullable Direction faceFromIndex(int faceIndex) {
        return GTUtil.DIRECTIONS_WITH_NULL[faceIndex];
    }

    /**
     * Identifies the face to which the quad is most closely aligned. This mimics the value that
     * {@link BakedQuad#getDirection()} returns, and is used in the vanilla renderer for all diffuse lighting.
     *
     * <p>
     * Derived from the quad face normal and expects convex quads with all points co-planar.
     */
    public static Direction lightFace(QuadView quad) {
        final Vector3f normal = quad.faceNormal();
        return switch (GeometryHelper.longestAxis(normal)) {
            case X -> normal.x() > 0 ? Direction.EAST : Direction.WEST;
            case Y -> normal.y() > 0 ? Direction.UP : Direction.DOWN;
            case Z -> normal.z() > 0 ? Direction.SOUTH : Direction.NORTH;
        };
    }

    /**
     * @see #longestAxis(float, float, float)
     */
    public static Axis longestAxis(Vector3f vec) {
        return longestAxis(vec.x(), vec.y(), vec.z());
    }

    /**
     * Identifies the largest (max absolute magnitude) component (X, Y, Z) in the given vector.
     */
    public static Axis longestAxis(float normalX, float normalY, float normalZ) {
        Axis result = Axis.Y;
        float longest = Math.abs(normalY);
        float a = Math.abs(normalX);

        if (a > longest) {
            result = Axis.X;
            longest = a;
        }

        return Math.abs(normalZ) > longest ? Axis.Z : result;
    }

    /**
     * Stores a normal plus an extra value as a quartet of signed bytes. This is the same normal format that vanilla
     * item rendering expects. The extra value is for use by shaders.
     */
    public static int packNormal(float x, float y, float z, float w) {
        x = Mth.clamp(x, -1, 1);
        y = Mth.clamp(y, -1, 1);
        z = Mth.clamp(z, -1, 1);
        w = Mth.clamp(w, -1, 1);

        return ((int) (x * 127) & 255) | (((int) (y * 127) & 255) << 8) | (((int) (z * 127) & 255) << 16) |
                (((int) (w * 127) & 255) << 24);
    }

    /**
     * Version of {@link #packNormal(float, float, float, float)} that accepts a vector type.
     */
    public static int packNormal(Vector3f normal, float w) {
        return packNormal(normal.x(), normal.y(), normal.z(), w);
    }

    /**
     * Retrieves values packed by {@link #packNormal(float, float, float, float)}.
     *
     * <p>
     * Components are x, y, z, w - zero based.
     */
    public static float getPackedNormalComponent(int packedNormal, int component) {
        return ((byte) (packedNormal >> (8 * component))) / 127f;
    }

    /**
     * Computes the face normal of the given quad and saves it in the provided non-null vector.
     * If {@link QuadView#nominalFace()} is set will optimize by confirming quad is parallel to that face and,
     * if so, use the standard normal for that face direction.
     *
     * <p>
     * Will work with triangles also. Assumes counter-clockwise winding order, which is the norm. Expects convex quads
     * with all points co-planar.
     */
    public static void computeFaceNormal(Vector3f saveTo, QuadView q) {
        final Direction nominalFace = q.nominalFace();

        if (isQuadParallelToFace(nominalFace, q)) {
            Vec3i vec = nominalFace.getNormal();
            saveTo.set(vec.getX(), vec.getY(), vec.getZ());
            return;
        }

        final float x0 = q.x(0);
        final float y0 = q.y(0);
        final float z0 = q.z(0);
        final float x1 = q.x(1);
        final float y1 = q.y(1);
        final float z1 = q.z(1);
        final float x2 = q.x(2);
        final float y2 = q.y(2);
        final float z2 = q.z(2);
        final float x3 = q.x(3);
        final float y3 = q.y(3);
        final float z3 = q.z(3);

        final float dx0 = x2 - x0;
        final float dy0 = y2 - y0;
        final float dz0 = z2 - z0;
        final float dx1 = x3 - x1;
        final float dy1 = y3 - y1;
        final float dz1 = z3 - z1;

        float normX = dy0 * dz1 - dz0 * dy1;
        float normY = dz0 * dx1 - dx0 * dz1;
        float normZ = dx0 * dy1 - dy0 * dx1;

        float l = (float) Math.sqrt(normX * normX + normY * normY + normZ * normZ);

        if (l != 0) {
            normX /= l;
            normY /= l;
            normZ /= l;
        }

        saveTo.set(normX, normY, normZ);
    }

    /**
     * Returns true if quad is parallel to the given face. Does not validate quad winding order. Expects convex quads
     * with all points co-planar.
     */
    public static boolean isQuadParallelToFace(@Nullable Direction face, QuadView quad) {
        if (face == null) {
            return false;
        }

        int i = face.getAxis().ordinal();
        final float val = quad.posByIndex(0, i);
        return Mth.equal(val, quad.posByIndex(1, i)) && Mth.equal(val, quad.posByIndex(2, i)) &&
                Mth.equal(val, quad.posByIndex(3, i));
    }
}
