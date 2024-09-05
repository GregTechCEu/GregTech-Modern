package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.client.renderer.pipe.quad.RecolorableBakedQuad;
import com.gregtechceu.gtceu.client.renderer.pipe.util.SpriteInformation;

import com.lowdragmc.lowdraglib.client.bakedpipeline.FaceQuad;

import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockElementRotation;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
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

    public static final int VERTEX_INT_SIZE = 8;
    private static final float RESCALE_22_5 = 1.0F / (float) Math.cos((float) (Math.PI / 8)) - 1.0F;
    private static final float RESCALE_45 = 1.0F / (float) Math.cos((float) (Math.PI / 4)) - 1.0F;
    public static final int VERTEX_COUNT = 4;
    private static final int COLOR_INDEX = 3;
    public static final int UV_INDEX = 4;

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
                new BlockElementFace(cull ? face : null, tintIndex, "",
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

    public static BakedQuad bakeQuad(
                                     Vector3f posFrom,
                                     Vector3f posTo,
                                     BlockElementFace face,
                                     TextureAtlasSprite sprite,
                                     Direction facing,
                                     ModelState transform,
                                     @Nullable BlockElementRotation partRotation,
                                     boolean shade,
                                     int emissivity) {
        BlockFaceUV blockfaceuv = face.uv;
        if (transform.isUvLocked()) {
            blockfaceuv = recomputeUVs(face.uv, facing, transform.getRotation());
        }

        float[] afloat = new float[blockfaceuv.uvs.length];
        System.arraycopy(blockfaceuv.uvs, 0, afloat, 0, afloat.length);
        float f = sprite.uvShrinkRatio();
        float f1 = (blockfaceuv.uvs[0] + blockfaceuv.uvs[0] + blockfaceuv.uvs[2] + blockfaceuv.uvs[2]) / VERTEX_COUNT;
        float f2 = (blockfaceuv.uvs[1] + blockfaceuv.uvs[1] + blockfaceuv.uvs[3] + blockfaceuv.uvs[3]) / VERTEX_COUNT;
        blockfaceuv.uvs[0] = Mth.lerp(f, blockfaceuv.uvs[0], f1);
        blockfaceuv.uvs[2] = Mth.lerp(f, blockfaceuv.uvs[2], f1);
        blockfaceuv.uvs[1] = Mth.lerp(f, blockfaceuv.uvs[1], f2);
        blockfaceuv.uvs[3] = Mth.lerp(f, blockfaceuv.uvs[3], f2);
        int[] aint = makeVertices(blockfaceuv, sprite, facing, setupShape(posFrom, posTo), transform.getRotation(),
                partRotation, shade);
        Direction direction = calculateFacing(aint);
        System.arraycopy(afloat, 0, blockfaceuv.uvs, 0, afloat.length);
        if (partRotation == null) {
            recalculateWinding(aint, direction);
        }

        ForgeHooksClient.fillNormal(aint, direction);
        ForgeFaceData data = face.getFaceData();
        BakedQuad quad = new BakedQuad(aint, face.tintIndex, direction, sprite, shade, data.ambientOcclusion());
        if (!ForgeFaceData.DEFAULT.equals(data)) {
            QuadTransformers.applyingLightmap(data.blockLight(), data.skyLight()).processInPlace(quad);
            QuadTransformers.applyingColor(data.color()).processInPlace(quad);
        }
        com.lowdragmc.lowdraglib.client.bakedpipeline.QuadTransformers.settingEmissivity(emissivity)
                .processInPlace(quad);

        return quad;
    }

    public static RecolorableBakedQuad bakeRecolorableQuad(
                                                           Vector3f posFrom,
                                                           Vector3f posTo,
                                                           BlockElementFace face,
                                                           SpriteInformation sprite,
                                                           Direction facing,
                                                           ModelState transform,
                                                           @Nullable BlockElementRotation partRotation,
                                                           boolean shade,
                                                           int emissivity) {
        BlockFaceUV blockfaceuv = face.uv;
        if (transform.isUvLocked()) {
            blockfaceuv = recomputeUVs(face.uv, facing, transform.getRotation());
        }

        float[] afloat = new float[blockfaceuv.uvs.length];
        System.arraycopy(blockfaceuv.uvs, 0, afloat, 0, afloat.length);
        float f = sprite.sprite().uvShrinkRatio();
        float f1 = (blockfaceuv.uvs[0] + blockfaceuv.uvs[0] + blockfaceuv.uvs[2] + blockfaceuv.uvs[2]) / VERTEX_COUNT;
        float f2 = (blockfaceuv.uvs[1] + blockfaceuv.uvs[1] + blockfaceuv.uvs[3] + blockfaceuv.uvs[3]) / VERTEX_COUNT;
        blockfaceuv.uvs[0] = Mth.lerp(f, blockfaceuv.uvs[0], f1);
        blockfaceuv.uvs[2] = Mth.lerp(f, blockfaceuv.uvs[2], f1);
        blockfaceuv.uvs[1] = Mth.lerp(f, blockfaceuv.uvs[1], f2);
        blockfaceuv.uvs[3] = Mth.lerp(f, blockfaceuv.uvs[3], f2);
        int[] aint = makeVertices(blockfaceuv, sprite.sprite(), facing, setupShape(posFrom, posTo),
                transform.getRotation(),
                partRotation, shade);
        Direction direction = calculateFacing(aint);
        System.arraycopy(afloat, 0, blockfaceuv.uvs, 0, afloat.length);
        if (partRotation == null) {
            recalculateWinding(aint, direction);
        }

        ForgeHooksClient.fillNormal(aint, direction);
        ForgeFaceData data = face.getFaceData();
        RecolorableBakedQuad quad = new RecolorableBakedQuad(
                new BakedQuad(aint, face.tintIndex, direction, sprite.sprite(), shade, data.ambientOcclusion()),
                sprite);
        if (!ForgeFaceData.DEFAULT.equals(data)) {
            QuadTransformers.applyingLightmap(data.blockLight(), data.skyLight()).processInPlace(quad);
            QuadTransformers.applyingColor(data.color()).processInPlace(quad);
        }
        com.lowdragmc.lowdraglib.client.bakedpipeline.QuadTransformers.settingEmissivity(emissivity)
                .processInPlace(quad);

        return quad;
    }

    public static BlockFaceUV recomputeUVs(BlockFaceUV uv, Direction facing, Transformation modelRotation) {
        Matrix4f matrix4f = BlockMath
                .getUVLockTransform(modelRotation, facing, () -> "Unable to resolve UVLock for model").getMatrix();
        float f = uv.getU(uv.getReverseIndex(0));
        float f1 = uv.getV(uv.getReverseIndex(0));
        Vector4f vector4f = matrix4f.transform(new Vector4f(f / 16.0F, f1 / 16.0F, 0.0F, 1.0F));
        float f2 = 16.0F * vector4f.x();
        float f3 = 16.0F * vector4f.y();
        float f4 = uv.getU(uv.getReverseIndex(2));
        float f5 = uv.getV(uv.getReverseIndex(2));
        Vector4f vector4f1 = matrix4f.transform(new Vector4f(f4 / 16.0F, f5 / 16.0F, 0.0F, 1.0F));
        float f6 = 16.0F * vector4f1.x();
        float f7 = 16.0F * vector4f1.y();
        float f8;
        float f9;
        if (Math.signum(f4 - f) == Math.signum(f6 - f2)) {
            f8 = f2;
            f9 = f6;
        } else {
            f8 = f6;
            f9 = f2;
        }

        float f10;
        float f11;
        if (Math.signum(f5 - f1) == Math.signum(f7 - f3)) {
            f10 = f3;
            f11 = f7;
        } else {
            f10 = f7;
            f11 = f3;
        }

        float f12 = (float) Math.toRadians(uv.rotation);
        Matrix3f matrix3f = new Matrix3f(matrix4f);
        Vector3f vector3f = matrix3f.transform(new Vector3f(Mth.cos(f12), Mth.sin(f12), 0.0F));
        int i = Math.floorMod(
                -((int) Math.round(Math.toDegrees(Math.atan2((double) vector3f.y(), (double) vector3f.x())) / 90.0)) *
                        90,
                360);
        return new BlockFaceUV(new float[] { f8, f10, f9, f11 }, i);
    }

    private static int[] makeVertices(
                                      BlockFaceUV uvs,
                                      TextureAtlasSprite sprite,
                                      Direction orientation,
                                      float[] posDiv16,
                                      Transformation rotation,
                                      @Nullable BlockElementRotation partRotation,
                                      boolean shade) {
        int[] aint = new int[32];

        for (int i = 0; i < 4; ++i) {
            bakeVertex(aint, i, orientation, uvs, posDiv16, sprite, rotation, partRotation, shade);
        }

        return aint;
    }

    private static void bakeVertex(
                                   int[] vertexData,
                                   int vertexIndex,
                                   Direction facing,
                                   BlockFaceUV blockFaceUV,
                                   float[] posDiv16,
                                   TextureAtlasSprite sprite,
                                   Transformation rotation,
                                   @Nullable BlockElementRotation partRotation,
                                   boolean shade) {
        FaceInfo.VertexInfo faceinfo$vertexinfo = FaceInfo.fromFacing(facing).getVertexInfo(vertexIndex);
        Vector3f vector3f = new Vector3f(posDiv16[faceinfo$vertexinfo.xFace], posDiv16[faceinfo$vertexinfo.yFace],
                posDiv16[faceinfo$vertexinfo.zFace]);
        applyElementRotation(vector3f, partRotation);
        applyModelRotation(vector3f, rotation);
        fillVertex(vertexData, vertexIndex, vector3f, sprite, blockFaceUV);
    }

    private static void fillVertex(int[] vertexData, int vertexIndex, Vector3f vector, TextureAtlasSprite sprite,
                                   BlockFaceUV blockFaceUV) {
        int i = vertexIndex * VERTEX_INT_SIZE;
        vertexData[i] = Float.floatToRawIntBits(vector.x());
        vertexData[i + 1] = Float.floatToRawIntBits(vector.y());
        vertexData[i + 2] = Float.floatToRawIntBits(vector.z());
        vertexData[i + COLOR_INDEX] = -1;
        vertexData[i + UV_INDEX] = Float.floatToRawIntBits(
                sprite.getU((double) blockFaceUV.getU(vertexIndex) * 0.999 +
                        (double) blockFaceUV.getU((vertexIndex + 2) % 4) * 0.001));
        vertexData[i + 4 + 1] = Float.floatToRawIntBits(
                sprite.getV((double) blockFaceUV.getV(vertexIndex) * 0.999 +
                        (double) blockFaceUV.getV((vertexIndex + 2) % 4) * 0.001));
    }

    private static float[] setupShape(Vector3f min, Vector3f max) {
        float[] afloat = new float[Direction.values().length];
        afloat[FaceInfo.Constants.MIN_X] = min.x() / 16.0F;
        afloat[FaceInfo.Constants.MIN_Y] = min.y() / 16.0F;
        afloat[FaceInfo.Constants.MIN_Z] = min.z() / 16.0F;
        afloat[FaceInfo.Constants.MAX_X] = max.x() / 16.0F;
        afloat[FaceInfo.Constants.MAX_Y] = max.y() / 16.0F;
        afloat[FaceInfo.Constants.MAX_Z] = max.z() / 16.0F;
        return afloat;
    }

    private static void applyElementRotation(Vector3f vec,
                                             @Nullable BlockElementRotation partRotation) {
        if (partRotation != null) {
            Vector3f vector3f;
            Vector3f vector3f1;
            switch (partRotation.axis()) {
                case X -> {
                    vector3f = new Vector3f(1.0F, 0.0F, 0.0F);
                    vector3f1 = new Vector3f(0.0F, 1.0F, 1.0F);
                }
                case Y -> {
                    vector3f = new Vector3f(0.0F, 1.0F, 0.0F);
                    vector3f1 = new Vector3f(1.0F, 0.0F, 1.0F);
                }
                case Z -> {
                    vector3f = new Vector3f(0.0F, 0.0F, 1.0F);
                    vector3f1 = new Vector3f(1.0F, 1.0F, 0.0F);
                }
                default -> throw new IllegalArgumentException("There are only 3 axes");
            }

            Quaternionf quaternionf = new Quaternionf().rotationAxis(partRotation.angle() * (float) (Math.PI / 180.0),
                    vector3f);
            if (partRotation.rescale()) {
                if (Math.abs(partRotation.angle()) == 22.5F) {
                    vector3f1.mul(RESCALE_22_5);
                } else {
                    vector3f1.mul(RESCALE_45);
                }

                vector3f1.add(1.0F, 1.0F, 1.0F);
            } else {
                vector3f1.set(1.0F, 1.0F, 1.0F);
            }

            rotateVertexBy(vec, new Vector3f(partRotation.origin()), new Matrix4f().rotation(quaternionf), vector3f1);
        }
    }

    public static void applyModelRotation(Vector3f pos, Transformation transform) {
        if (transform != Transformation.identity()) {
            rotateVertexBy(pos, new Vector3f(0.5F, 0.5F, 0.5F), transform.getMatrix(), new Vector3f(1.0F, 1.0F, 1.0F));
        }
    }

    private static void rotateVertexBy(Vector3f pos, Vector3f origin, Matrix4f transform, Vector3f scale) {
        Vector4f vector4f = transform
                .transform(new Vector4f(pos.x() - origin.x(), pos.y() - origin.y(), pos.z() - origin.z(), 1.0F));
        vector4f.mul(new Vector4f(scale, 1.0F));
        pos.set(vector4f.x() + origin.x(), vector4f.y() + origin.y(), vector4f.z() + origin.z());
    }

    private static void recalculateWinding(int[] vertices, Direction direction) {
        int[] newVertices = new int[vertices.length];
        System.arraycopy(vertices, 0, newVertices, 0, vertices.length);
        float[] normals = new float[Direction.values().length];
        normals[FaceInfo.Constants.MIN_X] = 999.0F;
        normals[FaceInfo.Constants.MIN_Y] = 999.0F;
        normals[FaceInfo.Constants.MIN_Z] = 999.0F;
        normals[FaceInfo.Constants.MAX_X] = -999.0F;
        normals[FaceInfo.Constants.MAX_Y] = -999.0F;
        normals[FaceInfo.Constants.MAX_Z] = -999.0F;

        for (int i = 0; i < 4; ++i) {
            int j = 8 * i;
            float f = Float.intBitsToFloat(newVertices[j]);
            float f1 = Float.intBitsToFloat(newVertices[j + 1]);
            float f2 = Float.intBitsToFloat(newVertices[j + 2]);
            if (f < normals[FaceInfo.Constants.MIN_X]) {
                normals[FaceInfo.Constants.MIN_X] = f;
            }

            if (f1 < normals[FaceInfo.Constants.MIN_Y]) {
                normals[FaceInfo.Constants.MIN_Y] = f1;
            }

            if (f2 < normals[FaceInfo.Constants.MIN_Z]) {
                normals[FaceInfo.Constants.MIN_Z] = f2;
            }

            if (f > normals[FaceInfo.Constants.MAX_X]) {
                normals[FaceInfo.Constants.MAX_X] = f;
            }

            if (f1 > normals[FaceInfo.Constants.MAX_Y]) {
                normals[FaceInfo.Constants.MAX_Y] = f1;
            }

            if (f2 > normals[FaceInfo.Constants.MAX_Z]) {
                normals[FaceInfo.Constants.MAX_Z] = f2;
            }
        }

        FaceInfo faceinfo = FaceInfo.fromFacing(direction);

        for (int i1 = 0; i1 < 4; ++i1) {
            int j1 = 8 * i1;
            FaceInfo.VertexInfo vertexInfo = faceinfo.getVertexInfo(i1);
            float x = normals[vertexInfo.xFace];
            float y = normals[vertexInfo.yFace];
            float z = normals[vertexInfo.zFace];
            vertices[j1] = Float.floatToRawIntBits(x);
            vertices[j1 + 1] = Float.floatToRawIntBits(y);
            vertices[j1 + 2] = Float.floatToRawIntBits(z);

            for (int k = 0; k < 4; ++k) {
                int l = 8 * k;
                float nX = Float.intBitsToFloat(newVertices[l]);
                float xY = Float.intBitsToFloat(newVertices[l + 1]);
                float nZ = Float.intBitsToFloat(newVertices[l + 2]);
                // noinspection SuspiciousNameCombination
                if (Mth.equal(x, nX) && Mth.equal(y, xY) && Mth.equal(z, nZ)) {
                    vertices[j1 + 4] = newVertices[l + 4];
                    vertices[j1 + 4 + 1] = newVertices[l + 4 + 1];
                }
            }
        }
    }
}
