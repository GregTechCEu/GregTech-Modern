/*
 * This file is part of ConnectedTexturesMod (https://github.com/Chisel-Team/ConnectedTexturesMod).
 * Copyright (c) 2023 Chisel Team.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * ConnectedTexturesMod is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with ConnectedTexturesMod; if not, If not, see <http://www.gnu.org/licenses/>.
 */
package com.gregtechceu.gtceu.client.model.ctm;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import lombok.RequiredArgsConstructor;

/**
 * Sourced from <a href=
 * "https://github.com/Chisel-Team/ConnectedTexturesMod/blob/19a58b080ff2d4fec4fd44ffdb426fc078ce853d/src/main/java/team/chisel/ctm/api/texture/ISubmap.java">ConnectedTexturesMod</a>.
 */
public interface ISubmap {

    float getYOffset();

    float getXOffset();

    float getWidth();

    float getHeight();

    default float getInterpolatedU(TextureAtlasSprite sprite, float u) {
        return sprite.getU((getXOffset() + u / getWidth()) / 16F);
    }

    default float getInterpolatedV(TextureAtlasSprite sprite, float v) {
        return sprite.getV((getYOffset() + v / getHeight()) / 16F);
    }

    default float[] toArray() {
        return new float[] { getXOffset(), getYOffset(), getXOffset() + getWidth(), getYOffset() + getHeight() };
    }

    default ISubmap unitScale() {
        return new SubmapRescaled(this, UNITS_PER_PIXEL, false);
    }

    default ISubmap pixelScale() {
        return this;
    }

    float PIXELS_PER_UNIT = 16f;
    float UNITS_PER_PIXEL = 1f / PIXELS_PER_UNIT;

    @RequiredArgsConstructor
    class SubmapRescaled implements ISubmap {

        private final ISubmap parent;
        private final float ratio;
        private final boolean isPixelScale;

        @Override
        public float getXOffset() {
            return parent.getXOffset() * ratio;
        }

        @Override
        public float getYOffset() {
            return parent.getYOffset() * ratio;
        }

        @Override
        public float getWidth() {
            return parent.getWidth() * ratio;
        }

        @Override
        public float getHeight() {
            return parent.getHeight() * ratio;
        }

        @Override
        public ISubmap pixelScale() {
            return isPixelScale ? this : parent;
        }

        @Override
        public ISubmap unitScale() {
            return isPixelScale ? parent : this;
        }

        @Override
        public float getInterpolatedU(TextureAtlasSprite sprite, float u) {
            return parent.getInterpolatedU(sprite, u);
        }

        @Override
        public float getInterpolatedV(TextureAtlasSprite sprite, float v) {
            return parent.getInterpolatedV(sprite, v);
        }

        @Override
        public float[] toArray() {
            return parent.toArray();
        }
    }

    default ISubmap flipX() {
        return Submap.fromPixelScale(getWidth(), getHeight(), PIXELS_PER_UNIT - getXOffset() - getWidth(),
                getYOffset());
    }

    default ISubmap flipY() {
        return Submap.fromPixelScale(getWidth(), getHeight(), getXOffset(),
                PIXELS_PER_UNIT - getYOffset() - getHeight());
    }
}
