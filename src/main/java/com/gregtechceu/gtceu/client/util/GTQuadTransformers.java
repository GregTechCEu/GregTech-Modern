package com.gregtechceu.gtceu.client.util;

import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.IQuadTransformer;
import net.minecraftforge.client.model.QuadTransformers;

import java.lang.reflect.Array;

public final class GTQuadTransformers {

    public static IQuadTransformer offset(float by) {
        if (by == 0.0f) return QuadTransformers.empty();

        return quad -> {
            int[] vertices = quad.getVertices();
            Direction direction = quad.getDirection();

            for (int i = 0; i < 4; i++) {
                int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.POSITION;
                float x = Float.intBitsToFloat(vertices[offset]);
                float y = Float.intBitsToFloat(vertices[offset + 1]);
                float z = Float.intBitsToFloat(vertices[offset + 2]);
                x += by * direction.getStepX();
                y += by * direction.getStepY();
                z += by * direction.getStepZ();

                vertices[offset] = Float.floatToRawIntBits(x);
                vertices[offset + 1] = Float.floatToRawIntBits(y);
                vertices[offset + 2] = Float.floatToRawIntBits(z);
            }
        };
    }

    public static BakedQuad setSprite(BakedQuad quad, TextureAtlasSprite newSprite) {
        TextureAtlasSprite oldSprite = quad.getSprite();
        int[] vertices = quad.getVertices().clone();

        for (int i = 0; i < 4; i++) {
            int offset = i * IQuadTransformer.STRIDE + IQuadTransformer.UV0;
            float u = Float.intBitsToFloat(vertices[offset]);
            float v = Float.intBitsToFloat(vertices[offset + 1]);
            float newU = GTMath.mapRange(oldSprite.getU0(), oldSprite.getU1(), newSprite.getU0(), newSprite.getU1(), u);
            float newV = GTMath.mapRange(oldSprite.getV0(), oldSprite.getV1(), newSprite.getV0(), newSprite.getV1(), v);

            vertices[offset] = Float.floatToRawIntBits(newU);
            vertices[offset + 1] = Float.floatToRawIntBits(newV);
        }
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(),
                newSprite, quad.isShade(), quad.hasAmbientOcclusion());
    }

    public static BakedQuad copy(BakedQuad quad) {
        return new BakedQuad(quad.getVertices().clone(), quad.getTintIndex(), quad.getDirection(),
                quad.getSprite(), quad.isShade(), quad.hasAmbientOcclusion());
    }

    private GTQuadTransformers() {}
}
