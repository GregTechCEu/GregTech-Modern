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

import com.gregtechceu.gtceu.client.util.quad.GeometryHelper;

import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.IQuadTransformer;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Holds all the array offsets and bit-wise encoders/decoders for packing/unpacking quad data in an array of integers.
 * All of this is implementation-specific - that's why it isn't a "helper" class.
 */
@SuppressWarnings("PointlessArithmeticExpression")
@UtilityClass
public class EncodingFormat {

    static final int VERTEX_X = IQuadTransformer.POSITION + 0;
    static final int VERTEX_Y = IQuadTransformer.POSITION + 1;
    static final int VERTEX_Z = IQuadTransformer.POSITION + 2;
    static final int VERTEX_COLOR = IQuadTransformer.COLOR;
    static final int VERTEX_U = IQuadTransformer.UV0 + 0;
    static final int VERTEX_V = IQuadTransformer.UV0 + 1;
    static final int VERTEX_LIGHTMAP = IQuadTransformer.UV2;
    static final int VERTEX_NORMAL = IQuadTransformer.NORMAL;
    public static final int VERTEX_STRIDE = IQuadTransformer.STRIDE;

    public static final int QUAD_STRIDE = VERTEX_STRIDE * 4;

    /** used for quick clearing of quad buffers. */
    static final int[] EMPTY = new int[QUAD_STRIDE];

    private static final int DIRECTION_MASK = Mth.smallestEncompassingPowerOfTwo(GeometryHelper.NULL_FACE_ID) - 1;
    private static final int DIRECTION_BIT_COUNT = Integer.bitCount(DIRECTION_MASK);
    private static final int CULL_SHIFT = 0;
    private static final int CULL_INVERSE_MASK = ~(DIRECTION_MASK << CULL_SHIFT);
    private static final int LIGHT_SHIFT = CULL_SHIFT + DIRECTION_BIT_COUNT;
    private static final int LIGHT_INVERSE_MASK = ~(DIRECTION_MASK << LIGHT_SHIFT);
    private static final int NORMALS_SHIFT = LIGHT_SHIFT + DIRECTION_BIT_COUNT;
    private static final int NORMALS_COUNT = 4;
    private static final int NORMALS_MASK = (1 << NORMALS_COUNT) - 1;
    private static final int NORMALS_INVERSE_MASK = ~(NORMALS_MASK << NORMALS_SHIFT);

    static @Nullable Direction cullFace(long bits) {
        return GeometryHelper.faceFromIndex((int) ((bits >> CULL_SHIFT) & DIRECTION_MASK));
    }

    static long cullFace(long bits, @Nullable Direction face) {
        return (bits & CULL_INVERSE_MASK) | (GeometryHelper.toFaceIndex(face) << CULL_SHIFT);
    }

    static Direction lightFace(long bits) {
        return Objects.requireNonNull(GeometryHelper.faceFromIndex((int) ((bits >> LIGHT_SHIFT) & DIRECTION_MASK)));
    }

    static long lightFace(long bits, Direction face) {
        return (bits & LIGHT_INVERSE_MASK) | ((long) GeometryHelper.toFaceIndex(face) << LIGHT_SHIFT);
    }

    /** indicate if vertex normal has been set - bits correspond to vertex ordinals. */
    static int normalFlags(long bits) {
        return (int) ((bits >> NORMALS_SHIFT) & NORMALS_MASK);
    }

    static long normalFlags(long bits, int normalFlags) {
        return (bits & NORMALS_INVERSE_MASK) | ((long) (normalFlags & NORMALS_MASK) << NORMALS_SHIFT);
    }
}
