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

import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Indexes a texture atlas to allow fast lookup of Sprites from baked vertex coordinates.
 * Main use is for {@link Mesh}-based models to generate vanilla quads on demand
 * without tracking and retaining the sprites that were baked into the mesh.
 * In other words, this class supplies the sprite parameter for
 * {@link QuadView#toBakedQuad(TextureAtlasSprite)}.
 *
 * @implNote Implementation is a straightforward quad tree.
 *           Other options that were considered were linear search (slow) and direct indexing of fixed-size cells.
 *           Direct indexing would be fastest but would be memory-intensive for large atlases
 *           and unsuitable for any atlas that isn't consistently aligned to a fixed cell size.
 */
public class SpriteFinder {

    /**
     * Retrieves or creates the finder for the given atlas. Instances should not be retained as fields or they must be
     * refreshed whenever there is a resource reload or other event that causes atlas textures to be re-stitched.
     */
    public static SpriteFinder get(TextureAtlas atlas) {
        return ((SpriteFinderAccess) atlas).gtceu$spriteFinder();
    }

    private final Node root;
    private final TextureAtlas textureAtlas;

    public SpriteFinder(Map<ResourceLocation, TextureAtlasSprite> sprites, TextureAtlas textureAtlas) {
        root = new Node(0.5f, 0.5f, 0.25f);
        this.textureAtlas = textureAtlas;
        sprites.values().forEach(root::add);
    }

    /**
     * Finds the atlas sprite containing the vertex centroid of the quad. Vertex centroid is essentially the mean u,v
     * coordinate - the intent being to find a point that is unambiguously inside the sprite (vs on an edge.)
     *
     * <p>
     * Should be reliable for any convex quad or triangle. May fail for non-convex quads. Note that all the above refers
     * to u,v coordinates. Geometric vertex does not matter, except to the extent it was used to determine u,v.
     */
    public TextureAtlasSprite find(QuadView quad) {
        float u = 0;
        float v = 0;

        for (int i = 0; i < 4; i++) {
            u += quad.u(i);
            v += quad.v(i);
        }

        return find(u * 0.25f, v * 0.25f);
    }

    /**
     * Alternative to {@link #find(QuadView)} when vertex centroid is already known or unsuitable.
     * Expects normalized (0-1) coordinates on the atlas texture, which should already be the case for u,v values
     * in vanilla baked quads and in {@link QuadView} after calling
     * {@link MutableQuadView#spriteBake(TextureAtlasSprite, int)}.
     *
     * <p>
     * Coordinates must be in the sprite interior for reliable results.
     * Generally it'll be easier to use {@link #find(QuadView)} unless you know
     * the vertex centroid will somehow not be in the quad interior.
     * This method will be slightly faster if you already have the centroid or another appropriate value.
     */
    public TextureAtlasSprite find(float u, float v) {
        return root.find(u, v);
    }

    private class Node {

        private final float midU;
        private final float midV;
        private final float cellRadius;

        private @Nullable Object lowLow = null;
        private @Nullable Object lowHigh = null;
        private @Nullable Object highLow = null;
        private @Nullable Object highHigh = null;

        Node(float midU, float midV, float radius) {
            this.midU = midU;
            this.midV = midV;
            cellRadius = radius;
        }

        static final float EPS = Mth.EPSILON;

        void add(TextureAtlasSprite sprite) {
            final boolean lowU = sprite.getU0() < midU - EPS;
            final boolean highU = sprite.getU1() > midU + EPS;
            final boolean lowV = sprite.getV0() < midV - EPS;
            final boolean highV = sprite.getV1() > midV + EPS;

            if (lowU && lowV) {
                addInner(sprite, lowLow, -1, -1, q -> lowLow = q);
            }

            if (lowU && highV) {
                addInner(sprite, lowHigh, -1, 1, q -> lowHigh = q);
            }

            if (highU && lowV) {
                addInner(sprite, highLow, 1, -1, q -> highLow = q);
            }

            if (highU && highV) {
                addInner(sprite, highHigh, 1, 1, q -> highHigh = q);
            }
        }

        private void addInner(TextureAtlasSprite sprite, @Nullable Object quadrant, int uStep, int vStep,
                              Consumer<Object> setter) {
            if (quadrant == null) {
                setter.accept(sprite);
            } else if (quadrant instanceof Node node) {
                node.add(sprite);
            } else {
                Node n = new Node(midU + cellRadius * uStep, midV + cellRadius * vStep, cellRadius * 0.5f);

                if (quadrant instanceof TextureAtlasSprite quadrantSprite) {
                    n.add(quadrantSprite);
                }

                n.add(sprite);
                setter.accept(n);
            }
        }

        private TextureAtlasSprite find(float u, float v) {
            if (u < midU) {
                return v < midV ? findInner(lowLow, u, v) : findInner(lowHigh, u, v);
            } else {
                return v < midV ? findInner(highLow, u, v) : findInner(highHigh, u, v);
            }
        }

        private TextureAtlasSprite findInner(@Nullable Object quadrant, float u, float v) {
            if (quadrant instanceof TextureAtlasSprite sprite) {
                return sprite;
            } else if (quadrant instanceof Node node) {
                return node.find(u, v);
            } else {
                return textureAtlas.getSprite(MissingTextureAtlasSprite.getLocation());
            }
        }
    }

    public interface SpriteFinderAccess {

        SpriteFinder gtceu$spriteFinder();
    }
}
