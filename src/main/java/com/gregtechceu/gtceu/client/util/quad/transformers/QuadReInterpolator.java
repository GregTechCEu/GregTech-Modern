/*
 * This file is part of CodeChickenLib.
 * Copyright (c) 2018, covers1624, All rights reserved.
 *
 * CodeChickenLib is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * CodeChickenLib is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CodeChickenLib. If not, see <http://www.gnu.org/licenses/lgpl>.
 */
package com.gregtechceu.gtceu.client.util.quad.transformers;

import com.gregtechceu.gtceu.client.model.quad.MutableQuadView;
import com.gregtechceu.gtceu.client.model.quad.QuadView;
import com.gregtechceu.gtceu.client.model.quad.transform.QuadTransform;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.Direction;

/**
 * This transformer Re-Interpolates the Color, UV's and LightMaps. Use this after all transformations that translate
 * vertices in the pipeline.
 * <p>
 * This Transformation can only be used in the BakedPipeline.
 *
 * @author covers1624
 */
public class QuadReInterpolator implements QuadTransform {

    private final InterpolationHelper interpolationHelper = new InterpolationHelper();

    private final int[] originalSpriteColor = new int[4];
    private final float[] originalSpriteU = new float[4];
    private final float[] originalSpriteV = new float[4];
    private final int[] originalSpriteLightmap = new int[4];

    public QuadReInterpolator() {}

    public void setInputQuad(QuadView quad) {
        Direction.Axis axis = quad.nominalFace().getAxis();
        int xIndex = xCoord(axis);
        int yIndex = yCoord(axis);

        interpolationHelper.reset(
                quad.posByIndex(0, xIndex), quad.posByIndex(0, yIndex),
                quad.posByIndex(1, xIndex), quad.posByIndex(1, yIndex),
                quad.posByIndex(2, xIndex), quad.posByIndex(2, yIndex),
                quad.posByIndex(3, xIndex), quad.posByIndex(3, yIndex));

        // Save the original properties of the quad's vertices
        for (int v = 0; v < 4; v++) {
            originalSpriteColor[v] = quad.color(v);
            originalSpriteU[v] = quad.u(v);
            originalSpriteV[v] = quad.v(v);
            originalSpriteLightmap[v] = quad.lightmap(v);
        }

        // interpolationHelper.reset(
        // posCache[0][0], posCache[0][1],
        // posCache[1][0], posCache[1][1],
        // posCache[2][0], posCache[2][1],
        // posCache[3][0], posCache[3][1]);
    }

    @Override
    public boolean transform(MutableQuadView quad) {
        Direction.Axis axis = quad.nominalFace().getAxis();
        int xIndex = xCoord(axis);
        int yIndex = yCoord(axis);

        this.interpolationHelper.setup();
        for (int i = 0; i < 4; i++) {
            float x = quad.posByIndex(i, xIndex);
            float y = quad.posByIndex(i, yIndex);
            this.interpolationHelper.locate(x, y);
            interpolateColorFrom(quad, i);
            interpolateUVFrom(quad, i);
            interpolateLightmapFrom(quad, i);
        }
        return true;
    }

    /**
     * Interpolates the new color values for this Vertex using the others as a reference.
     */
    public void interpolateColorFrom(MutableQuadView quad, int vertexIndex) {
        int p1 = this.originalSpriteColor[0];
        int p2 = this.originalSpriteColor[1];
        int p3 = this.originalSpriteColor[2];
        int p4 = this.originalSpriteColor[3];
        if (p1 == p2 && p2 == p3 && p3 == p4) {
            return; // Don't bother for uniformly colored quads
        }

        // Interpolate each color component separately
        int color = 0;
        int mask = 0xFF;
        for (int i = 0; i < 4; i++) {
            int p1c = p1 & mask;
            int p2c = p2 & mask;
            int p3c = p3 & mask;
            int p4c = p4 & mask;
            int interpolated = (int) interpolationHelper.interpolate(p1c, p2c, p3c, p4c);
            color |= interpolated & mask;
            mask <<= 8;
        }

        quad.color(vertexIndex, color);
    }

    /**
     * Interpolates the new UV values for this Vertex using the others as a reference.
     */
    public void interpolateUVFrom(MutableQuadView quad, int vertexIndex) {
        float p1 = originalSpriteU[0];
        float p2 = originalSpriteU[1];
        float p3 = originalSpriteU[2];
        float p4 = originalSpriteU[3];
        float u = interpolationHelper.interpolate(p1, p2, p3, p4);

        p1 = originalSpriteV[0];
        p2 = originalSpriteV[1];
        p3 = originalSpriteV[2];
        p4 = originalSpriteV[3];
        float v = interpolationHelper.interpolate(p1, p2, p3, p4);
        quad.uv(vertexIndex, u, v);
    }

    /**
     * Interpolates the new LightMap values for this Vertex using the others as a reference.
     *
     * @return The same Vertex.
     */
    public void interpolateLightmapFrom(MutableQuadView quad, int vertexIndex) {
        int p1 = originalSpriteLightmap[0];
        int p2 = originalSpriteLightmap[1];
        int p3 = originalSpriteLightmap[2];
        int p4 = originalSpriteLightmap[3];
        if (p1 == p2 && p2 == p3 && p3 == p4) {
            return; // Don't bother for uniformly lit quads
        }

        // Interpolate both lightmap components separately
        int p1l = LightTexture.block(p1);
        int p2l = LightTexture.block(p2);
        int p3l = LightTexture.block(p3);
        int p4l = LightTexture.block(p4);
        int block = (int) interpolationHelper.interpolate(p1l, p2l, p3l, p4l);

        p1l = LightTexture.sky(p1);
        p2l = LightTexture.sky(p2);
        p3l = LightTexture.sky(p3);
        p4l = LightTexture.sky(p4);
        int sky = (int) interpolationHelper.interpolate(p1l, p2l, p3l, p4l);

        quad.lightmap(vertexIndex, block, sky);
    }

    /**
     * Gets the 2d X coordinate for the given axis.
     *
     * @param axis The axis. side >> 1
     * @return The x coordinate.
     */
    private static int xCoord(Direction.Axis axis) {
        if (axis != Direction.Axis.X) {
            return 0;
        } else {
            return 2;
        }
    }

    /**
     * Gets the 2d Y coordinate for the given axis.
     *
     * @param axis The axis.
     * @return The y coordinate.
     */
    private static int yCoord(Direction.Axis axis) {
        if (axis != Direction.Axis.Y) {
            return 1;
        } else {
            return 2;
        }
    }
}
