package com.gregtechceu.gtceu.client.util;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;

import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockMath;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.ForgeFaceData;
import net.minecraftforge.client.model.QuadTransformers;

import com.mojang.math.Transformation;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;

import static net.minecraft.client.renderer.block.model.FaceBakery.calculateFacing;

public class StaticFaceBakery {

    public static final AABB SLIGHTLY_OVER_BLOCK = new AABB(-0.001f, -0.001f, -0.001f,
            1.001f, 1.001f, 1.001f);
    public static final AABB OUTPUT_OVERLAY = new AABB(-.006f, -.006f, -.006f,
            1.006f, 1.006f, 1.006f);
    public static final AABB AUTO_OUTPUT_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);
    public static final AABB COVER_OVERLAY = new AABB(-.008f, -.008f, -.008f,
            1.008f, 1.008f, 1.008f);

    private static final int VERTEX_INT_SIZE = 8;
    private static final float RESCALE_22_5 = 1.0F / (float) Math.cos((float) (Math.PI / 8)) - 1.0F;
    private static final float RESCALE_45 = 1.0F / (float) Math.cos((float) (Math.PI / 4)) - 1.0F;
    private static final int VERTEX_COUNT = 4;
    private static final int POSITION_INDEX = 0;
    private static final int COLOR_INDEX = 3;
    private static final int UV_INDEX = 4;

    /**
     * bake a quad of specific face.
     * 
     * @param cube       cube model
     * @param face       face of the quad
     * @param sprite     texture
     * @param rotation   additional rotation
     * @param tintIndex  tint color index
     * @param emissivity emissivity
     * @param cull       whether cull the face
     * @param shade      whether shade the face
     */
    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite, ModelState rotation,
                                     int tintIndex, int emissivity, boolean cull, boolean shade) {
        return bakeQuad(
                new Vector3f((float) cube.minX * 16f, (float) cube.minY * 16f, (float) cube.minZ * 16f),
                new Vector3f((float) cube.maxX * 16f, (float) cube.maxY * 16f, (float) cube.maxZ * 16f),
                new BlockElementFace(cull ? face : null, tintIndex, sprite.contents().name().toString(),
                        new BlockFaceUV(new float[] { 0.0F, 0.0F, 16.0F, 16.0F }, 0)),
                sprite,
                face,
                rotation,
                null,
                shade,
                emissivity);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation, int tintIndex,
                                     int emissivity, boolean cull, boolean shade) {
        return bakeFace(FaceQuad.BLOCK, face, sprite, rotation, tintIndex, emissivity, cull, shade);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation, int tintIndex,
                                     int emissivity) {
        return bakeFace(face, sprite, rotation, tintIndex, emissivity, true, true);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation, int tintIndex) {
        return bakeFace(face, sprite, rotation, tintIndex, 0);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite, ModelState rotation) {
        return bakeFace(face, sprite, rotation, -1);
    }

    public static BakedQuad bakeFace(Direction face, TextureAtlasSprite sprite) {
        return bakeFace(face, sprite, BlockModelRotation.X0_Y0);
    }

    public static BakedQuad bakeFace(AABB cube, Direction face, TextureAtlasSprite sprite) {
        return bakeFace(cube, face, sprite, BlockModelRotation.X0_Y0, -1, 0, true, true);
    }

    public static BakedQuad bakeQuad(Vector3f posFrom,
                                     Vector3f posTo,
                                     BlockElementFace face,
                                     TextureAtlasSprite sprite,
                                     Direction facing,
                                     ModelState transform,
                                     @Nullable BlockElementRotation partRotation,
                                     boolean shade,
                                     int emissivity) {
        BlockFaceUV uvs = face.uv;
        if (transform.isUvLocked()) {
            uvs = recomputeUVs(face.uv, facing, transform.getRotation());
        }

        float[] originalUVs = new float[uvs.uvs.length];
        System.arraycopy(uvs.uvs, 0, originalUVs, 0, originalUVs.length);

        float shrinkRatio = sprite.uvShrinkRatio();
        float uMiddle = (uvs.uvs[0] * 2 + uvs.uvs[2] * 2) / VERTEX_COUNT;
        float vMiddle = (uvs.uvs[1] * 2 + uvs.uvs[3] * 2) / VERTEX_COUNT;
        uvs.uvs[0] = Mth.lerp(shrinkRatio, uvs.uvs[0], uMiddle);
        uvs.uvs[2] = Mth.lerp(shrinkRatio, uvs.uvs[2], uMiddle);
        uvs.uvs[1] = Mth.lerp(shrinkRatio, uvs.uvs[1], vMiddle);
        uvs.uvs[3] = Mth.lerp(shrinkRatio, uvs.uvs[3], vMiddle);

        int[] vertices = makeVertices(uvs, sprite, facing,
                setupShape(posFrom, posTo), transform.getRotation(), partRotation, shade);
        Direction direction = calculateFacing(vertices);
        System.arraycopy(originalUVs, 0, uvs.uvs, 0, originalUVs.length);
        if (partRotation == null) {
            recalculateWinding(vertices, direction);
        }

        ForgeHooksClient.fillNormal(vertices, direction);
        ForgeFaceData data = face.getFaceData();
        BakedQuad quad = new BakedQuad(vertices, face.tintIndex, direction, sprite, shade, data.ambientOcclusion());
        if (!ForgeFaceData.DEFAULT.equals(data)) {
            QuadTransformers.applyingLightmap(data.blockLight(), data.skyLight()).processInPlace(quad);
            QuadTransformers.applyingColor(data.color()).processInPlace(quad);
        }
        if (emissivity > 0) {
            QuadTransformers.settingEmissivity(emissivity).processInPlace(quad);
        }

        return quad.gtceu$setTextureKey(face.texture);
    }

    public static BlockFaceUV recomputeUVs(BlockFaceUV uv, Direction facing, Transformation modelRotation) {
        Matrix4f uvLock = BlockMath
                .getUVLockTransform(modelRotation, facing, () -> "Unable to resolve UVLock for model").getMatrix();
        float maybeUMin = uv.getU(uv.getReverseIndex(0));
        float maybeVMin = uv.getV(uv.getReverseIndex(0));
        Vector4f lockedUVMin = uvLock.transform(new Vector4f(maybeUMin / 16.0F, maybeVMin / 16.0F, 0.0F, 1.0F));
        float uMinScaled = 16.0F * lockedUVMin.x();
        float vMinScaled = 16.0F * lockedUVMin.y();
        float maybeUMax = uv.getU(uv.getReverseIndex(2));
        float maybeVMax = uv.getV(uv.getReverseIndex(2));
        Vector4f lockedUVMax = uvLock.transform(new Vector4f(maybeUMax / 16.0F, maybeVMax / 16.0F, 0.0F, 1.0F));
        float uMaxScaled = 16.0F * lockedUVMax.x();
        float vMaxScaled = 16.0F * lockedUVMax.y();
        float uMin;
        float uMax;
        if (Math.signum(maybeUMax - maybeUMin) == Math.signum(uMaxScaled - uMinScaled)) {
            uMin = uMinScaled;
            uMax = uMaxScaled;
        } else {
            uMin = uMaxScaled;
            uMax = uMinScaled;
        }

        float vMin;
        float vMax;
        if (Math.signum(maybeVMax - maybeVMin) == Math.signum(vMaxScaled - vMinScaled)) {
            vMin = vMinScaled;
            vMax = vMaxScaled;
        } else {
            vMin = vMaxScaled;
            vMax = vMinScaled;
        }

        float rotation = (float) Math.toRadians(uv.rotation);
        Matrix3f uvMat3 = new Matrix3f(uvLock);
        Vector3f rotVector = uvMat3.transform(new Vector3f(Mth.cos(rotation), Mth.sin(rotation), 0.0F));
        int rotationDegrees = Math.floorMod(
                -((int) Math.round(Math.toDegrees(Math.atan2(rotVector.y(), rotVector.x())) / 90.0)) * 90,
                360);
        return new BlockFaceUV(new float[] { uMin, vMin, uMax, vMax }, rotationDegrees);
    }

    private static int[] makeVertices(BlockFaceUV uvs, TextureAtlasSprite sprite,
                                      Direction orientation, float[] shape,
                                      Transformation rotation, @Nullable BlockElementRotation partRotation,
                                      boolean shade) {
        int[] vert = new int[32];
        for (int i = 0; i < 4; ++i) {
            bakeVertex(vert, i, orientation, uvs, sprite, shape, rotation, partRotation, shade);
        }
        return vert;
    }

    private static void bakeVertex(int[] vertexData, int vertexIndex, Direction facing,
                                   BlockFaceUV blockFaceUV, TextureAtlasSprite sprite, float[] shape,
                                   Transformation rotation, @Nullable BlockElementRotation partRotation,
                                   boolean shade) {
        FaceInfo.VertexInfo vertexInfo = FaceInfo.fromFacing(facing).getVertexInfo(vertexIndex);
        Vector3f face = new Vector3f(shape[vertexInfo.xFace], shape[vertexInfo.yFace], shape[vertexInfo.zFace]);
        applyElementRotation(face, partRotation);
        applyModelRotation(face, rotation);
        fillVertex(vertexData, vertexIndex, face, sprite, blockFaceUV);
    }

    private static void fillVertex(int[] vertexData, int vertexIndex, Vector3f face,
                                   TextureAtlasSprite sprite, BlockFaceUV blockFaceUV) {
        int i = vertexIndex * VERTEX_INT_SIZE;
        vertexData[i + POSITION_INDEX] = Float.floatToRawIntBits(face.x());
        vertexData[i + POSITION_INDEX + 1] = Float.floatToRawIntBits(face.y());
        vertexData[i + POSITION_INDEX + 2] = Float.floatToRawIntBits(face.z());
        vertexData[i + COLOR_INDEX] = 0xffffffff;
        vertexData[i + UV_INDEX] = Float.floatToRawIntBits(
                sprite.getU(blockFaceUV.getU(vertexIndex) * 0.999 + blockFaceUV.getU((vertexIndex + 2) % 4) * 0.001));
        vertexData[i + UV_INDEX + 1] = Float.floatToRawIntBits(
                sprite.getV(blockFaceUV.getV(vertexIndex) * 0.999 + blockFaceUV.getV((vertexIndex + 2) % 4) * 0.001));
    }

    private static float[] setupShape(Vector3f min, Vector3f max) {
        float[] shape = new float[Direction.values().length];
        shape[FaceInfo.Constants.MIN_X] = min.x() / 16.0F;
        shape[FaceInfo.Constants.MIN_Y] = min.y() / 16.0F;
        shape[FaceInfo.Constants.MIN_Z] = min.z() / 16.0F;
        shape[FaceInfo.Constants.MAX_X] = max.x() / 16.0F;
        shape[FaceInfo.Constants.MAX_Y] = max.y() / 16.0F;
        shape[FaceInfo.Constants.MAX_Z] = max.z() / 16.0F;
        return shape;
    }

    private static void applyElementRotation(Vector3f vec, @Nullable BlockElementRotation partRotation) {
        if (partRotation != null) {
            Vector3f axis;
            Vector3f scale;
            switch (partRotation.axis()) {
                case X -> {
                    axis = new Vector3f(1.0F, 0.0F, 0.0F);
                    scale = new Vector3f(0.0F, 1.0F, 1.0F);
                }
                case Y -> {
                    axis = new Vector3f(0.0F, 1.0F, 0.0F);
                    scale = new Vector3f(1.0F, 0.0F, 1.0F);
                }
                case Z -> {
                    axis = new Vector3f(0.0F, 0.0F, 1.0F);
                    scale = new Vector3f(1.0F, 1.0F, 0.0F);
                }
                default -> throw new IllegalArgumentException("There are only 3 axes");
            }

            Quaternionf rotation = new Quaternionf()
                    .rotationAxis(partRotation.angle() * (float) (Math.PI / 180.0), axis);
            if (partRotation.rescale()) {
                if (Math.abs(partRotation.angle()) == 22.5F) {
                    scale.mul(RESCALE_22_5);
                } else {
                    scale.mul(RESCALE_45);
                }
                scale.add(1.0F, 1.0F, 1.0F);
            } else {
                scale.set(1.0F, 1.0F, 1.0F);
            }

            rotateVertexBy(vec, new Vector3f(partRotation.origin()), new Matrix4f().rotation(rotation), scale);
        }
    }

    public static void applyModelRotation(Vector3f pos, Transformation transform) {
        if (transform != Transformation.identity()) {
            rotateVertexBy(pos, new Vector3f(0.5F, 0.5F, 0.5F), transform.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
        }
    }

    private static void rotateVertexBy(Vector3f pos, Vector3f origin, Matrix4f transform, Vector3f scale) {
        Vector4f transformed = new Vector4f(pos.x() - origin.x(), pos.y() - origin.y(), pos.z() - origin.z(), 1.0F)
                .mul(transform);
        transformed.mul(new Vector4f(scale, 1.0F));
        pos.set(transformed.x() + origin.x(), transformed.y() + origin.y(), transformed.z() + origin.z());
    }

    private static void recalculateWinding(int[] vertices, Direction direction) {
        int[] verticesCopy = new int[vertices.length];
        System.arraycopy(vertices, 0, verticesCopy, 0, vertices.length);
        float[] shape = new float[Direction.values().length];
        shape[FaceInfo.Constants.MIN_X] = 999.0F;
        shape[FaceInfo.Constants.MIN_Y] = 999.0F;
        shape[FaceInfo.Constants.MIN_Z] = 999.0F;
        shape[FaceInfo.Constants.MAX_X] = -999.0F;
        shape[FaceInfo.Constants.MAX_Y] = -999.0F;
        shape[FaceInfo.Constants.MAX_Z] = -999.0F;

        for (int i = 0; i < 4; ++i) {
            int element = 8 * i;
            float x = Float.intBitsToFloat(verticesCopy[element]);
            float y = Float.intBitsToFloat(verticesCopy[element + 1]);
            float z = Float.intBitsToFloat(verticesCopy[element + 2]);
            if (x < shape[FaceInfo.Constants.MIN_X]) {
                shape[FaceInfo.Constants.MIN_X] = x;
            }
            if (y < shape[FaceInfo.Constants.MIN_Y]) {
                shape[FaceInfo.Constants.MIN_Y] = y;
            }
            if (z < shape[FaceInfo.Constants.MIN_Z]) {
                shape[FaceInfo.Constants.MIN_Z] = z;
            }
            if (x > shape[FaceInfo.Constants.MAX_X]) {
                shape[FaceInfo.Constants.MAX_X] = x;
            }
            if (y > shape[FaceInfo.Constants.MAX_Y]) {
                shape[FaceInfo.Constants.MAX_Y] = y;
            }
            if (z > shape[FaceInfo.Constants.MAX_Z]) {
                shape[FaceInfo.Constants.MAX_Z] = z;
            }
        }

        FaceInfo faceInfo = FaceInfo.fromFacing(direction);

        for (int vert1 = 0; vert1 < 4; ++vert1) {
            int e1 = vert1 * VERTEX_INT_SIZE;
            FaceInfo.VertexInfo vertexInfo = faceInfo.getVertexInfo(vert1);
            float x1 = shape[vertexInfo.xFace];
            float y1 = shape[vertexInfo.yFace];
            float z1 = shape[vertexInfo.zFace];
            vertices[e1 + POSITION_INDEX] = Float.floatToRawIntBits(x1);
            vertices[e1 + POSITION_INDEX + 1] = Float.floatToRawIntBits(y1);
            vertices[e1 + POSITION_INDEX + 2] = Float.floatToRawIntBits(z1);

            for (int vert2 = 0; vert2 < 4; ++vert2) {
                int e2 = vert2 * VERTEX_INT_SIZE;
                float x2 = Float.intBitsToFloat(verticesCopy[e2 + POSITION_INDEX]);
                float y2 = Float.intBitsToFloat(verticesCopy[e2 + POSITION_INDEX + 1]);
                float z2 = Float.intBitsToFloat(verticesCopy[e2 + POSITION_INDEX + 2]);

                if (Mth.equal(x1, x2) && Mth.equal(y1, y2) && Mth.equal(z1, z2)) {
                    vertices[e1 + UV_INDEX] = verticesCopy[e2 + UV_INDEX];
                    vertices[e1 + UV_INDEX + 1] = verticesCopy[e2 + UV_INDEX + 1];
                }
            }
        }
    }
}
