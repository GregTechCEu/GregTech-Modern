package com.gregtechceu.gtceu.client.util;

import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;

public final class GTQuadTransformers {

    public static IQuadTransformer offset(float by) {
        return offset(by, by, by);
    }

    public static IQuadTransformer offset(float xOffset, float yOffset, float zOffset) {
        if (xOffset == 0.0f && yOffset == 0.0f && zOffset == 0.0f) return QuadTransformers.empty();

        return quad -> {
            int[] vertices = quad.getVertices();
            Direction direction = quad.getDirection();
            FaceInfo faceInfo = FaceInfo.fromFacing(direction);

            for (int i = 0; i < 4; i++) {
                FaceInfo.VertexInfo normal = faceInfo.getVertexInfo(i);
                int xNormal = Direction.from3DDataValue(normal.xFace).getStepX();
                int yNormal = Direction.from3DDataValue(normal.yFace).getStepY();
                int zNormal = Direction.from3DDataValue(normal.zFace).getStepZ();

                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);

                x += xOffset * xNormal;
                y += yOffset * yNormal;
                z += zOffset * zNormal;

                vertices[offset] = Float.floatToRawIntBits(x);
                vertices[offset + 1] = Float.floatToRawIntBits(y);
                vertices[offset + 2] = Float.floatToRawIntBits(z);
            }
        };
    }

    public static BakedQuad setSprite(BakedQuad quad, TextureAtlasSprite sprite) {
        TextureAtlasSprite oldSprite = quad.getSprite();
        int[] vertices = quad.getVertices().clone();

        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float u = Float.intBitsToFloat(vertices[offset]);
            float v = Float.intBitsToFloat(vertices[offset + 1]);

            // same as sprite.getX(oldSprite.getXOffset(x)), but we don't multiply and divide in between
            u = Mth.map(u, oldSprite.getU0(), oldSprite.getU1(), sprite.getU0(), sprite.getU1());
            v = Mth.map(v, oldSprite.getV0(), oldSprite.getV1(), sprite.getV0(), sprite.getV1());

            vertices[offset] = Float.floatToRawIntBits(u);
            vertices[offset + 1] = Float.floatToRawIntBits(v);
        }
        BakedQuad newQuad = new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(),
                sprite, quad.isShade(), quad.hasAmbientOcclusion());
        return newQuad.gtceu$setTextureKey(quad.gtceu$getTextureKey());
    }

    public static BakedQuad setColor(BakedQuad quad, int argbColor, boolean clearTintIndex) {
        int[] vertices = quad.getVertices().clone();
        BakedQuad copy = new BakedQuad(vertices, clearTintIndex ? -1 : quad.getTintIndex(), quad.getDirection(),
                quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());

        QuadTransformers.applyingColor(argbColor).processInPlace(copy);
        return copy.gtceu$setTextureKey(quad.gtceu$getTextureKey());
    }

    public static BakedQuad copy(BakedQuad quad) {
        return new BakedQuad(quad.getVertices().clone(), quad.getTintIndex(), quad.getDirection(),
                quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion())
                .gtceu$setTextureKey(quad.gtceu$getTextureKey());
    }

    private GTQuadTransformers() {}
}
