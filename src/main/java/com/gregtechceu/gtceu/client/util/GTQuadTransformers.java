package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.client.model.compat.IQuadTransformer;
import com.gregtechceu.gtceu.client.model.compat.QuadTransformers;
import com.gregtechceu.gtceu.core.IGTBakedQuad;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.client.model.quad.BakedColors;

import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class GTQuadTransformers {

    public static IQuadTransformer offset(float by) {
        return offset(by, by, by);
    }

    public static IQuadTransformer offset(float xOffset, float yOffset, float zOffset) {
        if (xOffset == 0.0f && yOffset == 0.0f && zOffset == 0.0f) return QuadTransformers.empty();

        return quads -> {
            for (int quadIndex = 0; quadIndex < quads.size(); quadIndex++) {
                BakedQuad quad = quads.get(quadIndex);
                Direction direction = quad.direction();
                FaceInfo faceInfo = FaceInfo.fromFacing(direction);
                Vector3f[] positions = new Vector3f[4];

                for (int i = 0; i < 4; i++) {
                    FaceInfo.VertexInfo normal = faceInfo.getVertexInfo(i);
                    int xNormal = directionStep(normal.xFace());
                    int yNormal = directionStep(normal.yFace());
                    int zNormal = directionStep(normal.zFace());

                    positions[i] = new Vector3f(quad.position(i))
                            .add(xOffset * xNormal, yOffset * yNormal, zOffset * zNormal);
                }

                quads.set(quadIndex, copyWithPositions(quad, positions));
            }
        };
    }

    public static BakedQuad setSprite(BakedQuad quad, TextureAtlasSprite sprite) {
        TextureAtlasSprite oldSprite = quad.materialInfo().sprite();
        long[] uvs = new long[4];

        for (int i = 0; i < 4; i++) {
            float u = UVPair.unpackU(quad.packedUV(i));
            float v = UVPair.unpackV(quad.packedUV(i));

            // same as sprite.getX(oldSprite.getXOffset(x)), but we don't multiply and divide in between
            u = Mth.map(u, oldSprite.getU0(), oldSprite.getU1(), sprite.getU0(), sprite.getU1());
            v = Mth.map(v, oldSprite.getV0(), oldSprite.getV1(), sprite.getV0(), sprite.getV1());

            uvs[i] = UVPair.pack(u, v);
        }
        BakedQuad.MaterialInfo oldInfo = quad.materialInfo();
        BakedQuad.MaterialInfo newInfo = new BakedQuad.MaterialInfo(sprite, oldInfo.layer(), oldInfo.itemRenderType(),
                normalizeLegacyTintIndex(oldInfo.tintIndex()), oldInfo.shade(), oldInfo.lightEmission(),
                oldInfo.ambientOcclusion());
        return copyWith(quad, positions(quad), uvs, newInfo, quad.bakedColors());
    }

    public static BakedQuad setColor(BakedQuad quad, int argbColor, boolean clearTintIndex) {
        BakedQuad.MaterialInfo oldInfo = quad.materialInfo();
        BakedQuad.MaterialInfo newInfo = new BakedQuad.MaterialInfo(oldInfo.sprite(), oldInfo.layer(),
                oldInfo.itemRenderType(), clearTintIndex ? -1 : normalizeLegacyTintIndex(oldInfo.tintIndex()),
                oldInfo.shade(), oldInfo.lightEmission(), oldInfo.ambientOcclusion());
        BakedColors colors = BakedColors.of(
                ARGB.multiply(argbColor, quad.bakedColors().color(0)),
                ARGB.multiply(argbColor, quad.bakedColors().color(1)),
                ARGB.multiply(argbColor, quad.bakedColors().color(2)),
                ARGB.multiply(argbColor, quad.bakedColors().color(3)));
        return copyWith(quad, positions(quad), uvs(quad), newInfo, colors);
    }

    public static BakedQuad copy(BakedQuad quad) {
        return copyWith(quad, positions(quad), uvs(quad), quad.materialInfo(), quad.bakedColors());
    }

    public static BakedQuad normalizeLegacyTintIndex(BakedQuad quad) {
        BakedQuad.MaterialInfo oldInfo = quad.materialInfo();
        int tintIndex = normalizeLegacyTintIndex(oldInfo.tintIndex());
        if (tintIndex == oldInfo.tintIndex()) {
            return quad;
        }
        BakedQuad.MaterialInfo newInfo = new BakedQuad.MaterialInfo(oldInfo.sprite(), oldInfo.layer(),
                oldInfo.itemRenderType(), tintIndex, oldInfo.shade(), oldInfo.lightEmission(),
                oldInfo.ambientOcclusion());
        return copyWith(quad, positions(quad), uvs(quad), newInfo, quad.bakedColors());
    }

    public static int normalizeLegacyTintIndex(int tintIndex) {
        // Legacy GT models used values below -100 to mark emissive
        // variants of normal tint layers. Vanilla 26.1 caches block
        // tint sources by raw index, so keep the layer and drop only
        // the old emissive marker.
        return tintIndex < -100 ? -tintIndex - 101 : tintIndex;
    }

    public static BakedQuad process(IQuadTransformer transformer, BakedQuad quad) {
        List<BakedQuad> quads = new ArrayList<>(List.of(quad));
        transformer.processInPlace(quads);
        return quads.getFirst();
    }

    private static BakedQuad copyWithPositions(BakedQuad quad, Vector3f[] positions) {
        return copyWith(quad, positions, uvs(quad), quad.materialInfo(), quad.bakedColors());
    }

    private static BakedQuad copyWith(BakedQuad quad, Vector3f[] positions, long[] uvs,
                                      BakedQuad.MaterialInfo materialInfo, BakedColors colors) {
        BakedQuad copy = new BakedQuad(positions[0], positions[1], positions[2], positions[3],
                uvs[0], uvs[1], uvs[2], uvs[3], quad.direction(), materialInfo, quad.bakedNormals(), colors);
        return ((IGTBakedQuad) (Object) copy).gtceu$setTextureKey(((IGTBakedQuad) (Object) quad).gtceu$getTextureKey());
    }

    private static Vector3f[] positions(BakedQuad quad) {
        return new Vector3f[] {
                new Vector3f(quad.position0()),
                new Vector3f(quad.position1()),
                new Vector3f(quad.position2()),
                new Vector3f(quad.position3())
        };
    }

    private static long[] uvs(BakedQuad quad) {
        return new long[] {
                quad.packedUV0(),
                quad.packedUV1(),
                quad.packedUV2(),
                quad.packedUV3()
        };
    }

    private static int directionStep(FaceInfo.Extent extent) {
        return switch (extent) {
            case MIN_X -> Direction.WEST.getStepX();
            case MAX_X -> Direction.EAST.getStepX();
            case MIN_Y -> Direction.DOWN.getStepY();
            case MAX_Y -> Direction.UP.getStepY();
            case MIN_Z -> Direction.NORTH.getStepZ();
            case MAX_Z -> Direction.SOUTH.getStepZ();
        };
    }

    private GTQuadTransformers() {}
}
