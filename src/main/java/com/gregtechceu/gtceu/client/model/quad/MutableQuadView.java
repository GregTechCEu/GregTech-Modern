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
package com.gregtechceu.gtceu.client.model.quad;

import com.gregtechceu.gtceu.client.util.TextureHelper;
import com.gregtechceu.gtceu.client.util.quad.GeometryHelper;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.QuadTransformers;

import org.jetbrains.annotations.Nullable;
import org.joml.Vector2fc;
import org.joml.Vector3f;

import static com.gregtechceu.gtceu.client.model.quad.EncodingFormat.*;

/**
 * A mutable {@link QuadView} instance.
 *
 * <p>
 * Instances of {@link MutableQuadView} will practically always be thread local and/or reused - do not retain
 * references.
 *
 * <p>
 * Only the renderer should implement or extend this interface.
 *
 * @implNote Almost-concrete implementation of a mutable quad.
 */
public abstract class MutableQuadView extends QuadView {

    /**
     * Causes texture to appear with no rotation.
     * Pass in bakeFlags parameter to {@link #spriteBake(TextureAtlasSprite, int)}.
     */
    public static final int BAKE_ROTATE_NONE = 0b000000;

    /**
     * Causes texture to appear rotated 90 deg. clockwise relative to nominal face. Pass in bakeFlags parameter to
     * {@link #spriteBake(TextureAtlasSprite, int)}.
     */
    public static final int BAKE_ROTATE_90 = 0b000001;

    /**
     * Causes texture to appear rotated 180 deg. relative to nominal face. Pass in bakeFlags parameter to
     * {@link #spriteBake(TextureAtlasSprite, int)}.
     */
    public static final int BAKE_ROTATE_180 = 0b000010;

    /**
     * Causes texture to appear rotated 270 deg. clockwise relative to nominal face. Pass in bakeFlags parameter to
     * {@link #spriteBake(TextureAtlasSprite, int)}.
     */
    public static final int BAKE_ROTATE_270 = 0b000011;

    /**
     * When enabled, texture coordinates are assigned based on vertex position.
     * Any existing UV coordinates will be replaced.
     * Pass in bakeFlags parameter to {@link #spriteBake(TextureAtlasSprite, int)}.
     *
     * <p>
     * UV lock always derives texture coordinates based on nominal face, even when the quad is not co-planar
     * with that face. The result is the same as if the quad were projected onto the nominal face, which is usually the
     * desired result.
     */
    public static final int BAKE_LOCK_UV = 0b000100;

    /**
     * When enabled, texture coordinates are cycled so that vertex 0's UVs are the smallest.
     * Pass in bakeFlags parameter to {@link TextureHelper#unbakeSprite(MutableQuadView, TextureAtlasSprite, int)}.
     */
    public static final int BAKE_DEROTATE_UV = 0b000100;

    /**
     * When set, U texture coordinates for the given sprite are flipped as part of baking. Can be useful for some
     * randomization and texture mapping scenarios. Results are different from what can be obtained via rotation and
     * both can be applied. Pass in bakeFlags parameter to {@link #spriteBake(TextureAtlasSprite, int)}.
     */
    public static final int BAKE_FLIP_U = 0b001000;

    /**
     * Same as {@link #BAKE_FLIP_U} but for V coordinate.
     */
    public static final int BAKE_FLIP_V = 0b010000;

    /**
     * UV coordinates by default are assumed to be 0-16 scale for consistency with conventional Minecraft model format.
     * This is scaled to 0-1 during baking before interpolation. Model loaders that already have 0-1 coordinates can
     * avoid wasteful multiplication/division by passing 0-1 coordinates directly.
     * Pass in bakeFlags parameter to {@link #spriteBake(TextureAtlasSprite, int)}.
     */
    public static final int BAKE_NORMALIZED = 0b100000;

    public static MutableQuadView getInstance() {
        return MutableQuadView.THREAD_LOCAL.get();
    }

    public static final ThreadLocal<MutableQuadView> THREAD_LOCAL = ThreadLocal
            .withInitial(() -> new MutableQuadView() {

                {
                    begin(new int[QUAD_STRIDE], 0);
                }

                @Override
                public MutableQuadView emit() {
                    throw new UnsupportedOperationException();
                }
            });

    public final void begin(int[] data, int baseIndex) {
        this.data = data;
        this.baseIndex = baseIndex;
        clear();
    }

    public void clear() {
        System.arraycopy(EMPTY, 0, data, baseIndex, QUAD_STRIDE);
        isGeometryInvalid = true;
        nominalFace = null;
        tintIndex(-1);
        cullFace(null);
    }

    /**
     * Sets the geometric vertex position for the given vertex, relative to block origin, (0,0,0).
     * Minecraft rendering is designed for models that fit within a single block space and is recommended
     * that coordinates remain in the 0-1 range, with multi-block meshes split into multiple per-block models.
     */
    public MutableQuadView pos(int vertexIndex, float x, float y, float z) {
        final int index = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X;
        data[index] = Float.floatToRawIntBits(x);
        data[index + 1] = Float.floatToRawIntBits(y);
        data[index + 2] = Float.floatToRawIntBits(z);
        isGeometryInvalid = true;
        return this;
    }

    /**
     * Same as {@link #pos(int, float, float, float)} but accepts a vector type.
     */
    public MutableQuadView pos(int vertexIndex, Vector3f pos) {
        return pos(vertexIndex, pos.x(), pos.y(), pos.z());
    }

    /**
     * Sets the vertex <strong>X</strong> position for the given vertex, relative to block origin, (0,0,0).
     * Minecraft rendering is designed for models that fit within a single block space and is recommended
     * that coordinates remain in the 0-1 range, with multi-block meshes split into multiple per-block models.
     */
    public MutableQuadView x(int vertexIndex, float x) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_X] = Float.floatToRawIntBits(x);
        isGeometryInvalid = true;
        return this;
    }

    /**
     * Sets the vertex <strong>Y</strong> position for the given vertex, relative to block origin, (0,0,0).
     * Minecraft rendering is designed for models that fit within a single block space and is recommended
     * that coordinates remain in the 0-1 range, with multi-block meshes split into multiple per-block models.
     */
    public MutableQuadView y(int vertexIndex, float y) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_Y] = Float.floatToRawIntBits(y);
        isGeometryInvalid = true;
        return this;
    }

    /**
     * Sets the vertex <strong>Z</strong> position for the given vertex, relative to block origin, (0,0,0).
     * Minecraft rendering is designed for models that fit within a single block space and is recommended
     * that coordinates remain in the 0-1 range, with multi-block meshes split into multiple per-block models.
     */
    public MutableQuadView z(int vertexIndex, float z) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_Z] = Float.floatToRawIntBits(z);
        isGeometryInvalid = true;
        return this;
    }

    /**
     * Set vertex color.
     */
    public MutableQuadView color(int vertexIndex, int color) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_COLOR] = color;
        return this;
    }

    /**
     * Convenience: set vertex color for all vertices at once.
     */
    public MutableQuadView color(int c0, int c1, int c2, int c3) {
        color(0, c0);
        color(1, c1);
        color(2, c2);
        color(3, c3);
        return this;
    }

    /**
     * Set texture coordinates.
     */
    public MutableQuadView uv(int vertexIndex, float u, float v) {
        final int i = baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_U;
        data[i] = Float.floatToRawIntBits(u);
        data[i + 1] = Float.floatToRawIntBits(v);
        return this;
    }

    /**
     * Set texture coordinates.
     *
     * <p>
     * Only use this function if you already have a {@link Vector2fc}.
     * Otherwise, see {@link MutableQuadView#uv(int, float, float)}.
     *
     * @see MutableQuadView#uv(int, float, float)
     */
    public MutableQuadView uv(int vertexIndex, Vector2fc uv) {
        return uv(vertexIndex, uv.x(), uv.y());
    }

    /**
     * Assigns sprite atlas u,v coordinates to this quad for the given sprite.
     * Can handle UV locking, rotation, interpolation, etc.
     * Control this behavior by passing additive combinations of the BAKE_ flags defined in this interface.
     */
    public MutableQuadView spriteBake(@Nullable TextureAtlasSprite sprite, int bakeFlags) {
        TextureHelper.bakeSprite(this, sprite, bakeFlags);
        return this;
    }

    /**
     * Normalizes this quad's u,v coordinates based on the given sprite.
     * Can handle UV rotation, interpolation, etc.
     * Control this behavior by passing additive combinations of the BAKE_ flags defined in this interface.
     */
    public MutableQuadView spriteUnbake(@Nullable TextureAtlasSprite sprite, int bakeFlags) {
        TextureHelper.unbakeSprite(this, sprite, bakeFlags);
        return this;
    }

    /**
     * Accept vanilla lightmap values.
     * Input values will override lightmap values computed from world state if input values are higher.
     */
    public MutableQuadView lightmap(int vertexIndex, int lightmap) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_LIGHTMAP] = lightmap;
        return this;
    }

    /**
     * Accept vanilla lightmap values.
     * Input values will override lightmap values computed from world state if input values are higher.
     */
    public MutableQuadView lightmap(int vertexIndex, int block, int sky) {
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_LIGHTMAP] = LightTexture.pack(block, sky);
        return this;
    }

    /**
     * Convenience: set lightmap for all vertices at once.
     *
     * @see #lightmap(int, int)
     */
    public MutableQuadView lightmap(int b0, int b1, int b2, int b3) {
        lightmap(0, b0);
        lightmap(1, b1);
        lightmap(2, b2);
        lightmap(3, b3);
        return this;
    }

    protected void normalFlags(int flags) {
        headerFlags = EncodingFormat.normalFlags(headerFlags, flags);
    }

    /**
     * Adds a vertex normal.
     * Models that have per-vertex normals should include them to get correct lighting when it matters.
     * Computed face normal is used when no vertex normal is provided.
     */
    public MutableQuadView normal(int vertexIndex, float x, float y, float z) {
        normalFlags(normalFlags() | (1 << vertexIndex));
        data[baseIndex + vertexIndex * VERTEX_STRIDE + VERTEX_NORMAL] = GeometryHelper.packNormal(x, y, z, 0);
        return this;
    }

    /**
     * Same as {@link #normal(int, float, float, float)} but accepts a vector type.
     */
    public MutableQuadView normal(int vertexIndex, Vector3f normal) {
        return normal(vertexIndex, normal.x(), normal.y(), normal.z());
    }

    /**
     * Internal helper method. Copies face normals to vertex normals lacking one.
     */
    public final void populateMissingNormals() {
        final int normalFlags = this.normalFlags();
        if (normalFlags == 0b1111)
            return;

        final int packedFaceNormal = GeometryHelper.packNormal(faceNormal(), 0);

        for (int v = 0; v < 4; v++) {
            if ((normalFlags & (1 << v)) == 0) {
                data[baseIndex + v * VERTEX_STRIDE + VERTEX_NORMAL] = packedFaceNormal;
            }
        }

        normalFlags(0b1111);
    }

    /**
     * If non-null, quad is coplanar with a block face which, if known, simplifies or shortcuts geometric analysis
     * that might otherwise be needed. Set to null if quad is not coplanar or if this is not known.
     * Also controls face culling during block rendering.
     *
     * <p>
     * Null by default.
     *
     * <p>
     * When called with a non-null value, also sets {@link #nominalFace(Direction)} to the same value.
     *
     * <p>
     * This is different from the value reported by {@link BakedQuad#getDirection()}. That value is computed based on
     * face geometry and must be non-null in vanilla quads. That computed value is returned by {@link #lightFace()}.
     */
    public final MutableQuadView cullFace(@Nullable Direction face) {
        headerFlags = EncodingFormat.cullFace(headerFlags, face);
        nominalFace(face);
        return this;
    }

    /**
     * Provides a hint to renderer about the facing of this quad.
     * Not required, but if provided can shortcut some geometric analysis if the quad is parallel to a block face.
     * Should be the expected value of {@link #lightFace()}.
     * Value will be confirmed and if invalid the correct light face will be calculated.
     *
     * <p>
     * Null by default, and set automatically by {@link #cullFace()}.
     *
     * <p>
     * Models may also find this useful as the face for texture UV locking and rotation semantics.
     *
     * <p>
     * Note: This value is not persisted independently when the quad is encoded.
     * When reading encoded quads, this value will always be the same as {@link #lightFace()}.
     */
    public final MutableQuadView nominalFace(@Nullable Direction face) {
        this.nominalFace = face;
        return this;
    }

    /**
     * Value functions identically to {@link BakedQuad#getTintIndex()}
     * and is used by renderer / model builder in same way.<br>
     * Default value is -1.
     */
    public final MutableQuadView tintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
        return this;
    }

    /**
     * Value functions identically to {@link BakedQuad#isShade()}
     * and is used by renderer / model builder in the same way.<br>
     * Default value is true.
     */
    public final MutableQuadView shade(boolean shade) {
        this.shade = shade;
        return this;
    }

    /**
     * Value functions identically to {@link BakedQuad#hasAmbientOcclusion()}
     * and is used by renderer / model builder in the same way.<br>
     * Default value is true.
     */
    public final MutableQuadView ambientOcclusion(boolean ao) {
        this.ambientOcclusion = ao;
        return this;
    }

    /**
     * Enables bulk vertex data transfer using the standard Minecraft vertex formats. Only the
     * {@link BakedQuad#getVertices() quad vertex data} is copied. This method should be performant whenever caller's
     * vertex representation makes it feasible.
     *
     * <p>
     * Use {@link #fromVanilla(BakedQuad, Direction) the other overload} which has better encapsulation
     * unless you have a specific reason to use this one.
     *
     * <p>
     * Calling this method does not emit the quad.
     */
    public final MutableQuadView fromVanilla(int[] quadData, int startIndex) {
        clear();
        System.arraycopy(quadData, startIndex, this.data, this.baseIndex, QUAD_STRIDE);
        this.isGeometryInvalid = true;

        int colorIndex = baseIndex + VERTEX_COLOR;

        for (int i = 0; i < 4; i++) {
            this.data[colorIndex] = QuadTransformers.toABGR(this.data[colorIndex]);
            colorIndex += VERTEX_STRIDE;
        }

        return this;
    }

    /**
     * Enables bulk vertex data transfer using the standard Minecraft quad format.
     *
     * <p>
     * Calling this method does not emit the quad.
     */
    public final MutableQuadView fromVanilla(BakedQuad quad, @Nullable Direction cullFace) {
        fromVanilla(quad.getVertices(), 0);
        headerFlags = EncodingFormat.cullFace(0, cullFace);

        nominalFace(quad.getDirection());
        tintIndex(quad.getTintIndex());
        shade(quad.isShade());
        ambientOcclusion(quad.hasAmbientOcclusion());

        return this;
    }

    /**
     * In static mesh building, causes quad to be appended to the mesh being built. In a dynamic render context, create
     * a new quad to be output to rendering. In both cases, current instance is reset to default values.
     */
    public abstract MutableQuadView emit();

    /**
     * Tolerance for determining if the depth parameter to {@link #square(Direction, float, float, float, float, float)}
     * is effectively zero - meaning the face is a cull face.
     */
    private static final float CULL_FACE_EPSILON = Mth.EPSILON;

    /**
     * Helper method to assign vertex coordinates for a square aligned with the given face.
     * Ensures that vertex order is consistent with vanilla convention.
     * (Incorrect order can lead to bad AO lighting unless enhanced lighting logic is available/enabled.)
     *
     * <p>
     * Square will be parallel to the given face and coplanar with the face (and culled if the face is occluded)
     * if the depth parameter is approximately zero. See {@link #CULL_FACE_EPSILON}.
     *
     * <p>
     * All coordinates should be normalized (0-1).
     */
    public MutableQuadView square(Direction nominalFace, float left, float bottom, float right, float top,
                                  float depth) {
        if (Math.abs(depth) < CULL_FACE_EPSILON) {
            cullFace(nominalFace);
            depth = 0; // avoid any inconsistency for face quads
        } else {
            cullFace(null);
        }

        nominalFace(nominalFace);
        switch (nominalFace) {
            case UP:
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;
                // Fallthrough

            case DOWN:
                pos(0, left, depth, top);
                pos(1, left, depth, bottom);
                pos(2, right, depth, bottom);
                pos(3, right, depth, top);
                break;

            case EAST:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;
                // Fallthrough

            case WEST:
                pos(0, depth, top, left);
                pos(1, depth, bottom, left);
                pos(2, depth, bottom, right);
                pos(3, depth, top, right);
                break;

            case SOUTH:
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;
                // Fallthrough

            case NORTH:
                pos(0, 1 - left, top, depth);
                pos(1, 1 - left, bottom, depth);
                pos(2, 1 - right, bottom, depth);
                pos(3, 1 - right, top, depth);
                break;
        }

        return this;
    }
}
