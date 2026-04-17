package com.gregtechceu.gtceu.client.model.connected;

import lombok.Getter;

/**
 * Sourced from <a href="https://github.com/Chisel-Team/ConnectedTexturesMod/blob/19a58b080ff2d4fec4fd44ffdb426fc078ce853d/src/main/java/team/chisel/ctm/client/util/Submap.java">ConnectedTexturesMod</a>.
 */
public class Submap implements ISubmap {
    
    public static final ISubmap X1 = fromPixelScale(16, 16, 0, 0);
    
    public static final ISubmap[][] X2 = {
        { fromPixelScale(8, 8, 0, 0), fromPixelScale(8, 8, 8, 0) },
        { fromPixelScale(8, 8, 0, 8), fromPixelScale(8, 8, 8, 8) }
    };
    
    private static final float DIV3 = 16 / 3f;
    public static final ISubmap[][] X3 = {
        { fromPixelScale(DIV3, DIV3, 0, 0),        fromPixelScale(DIV3, DIV3, DIV3, 0),        fromPixelScale(DIV3, DIV3, DIV3 * 2, 0) },
        { fromPixelScale(DIV3, DIV3, 0, DIV3),     fromPixelScale(DIV3, DIV3, DIV3, DIV3),     fromPixelScale(DIV3, DIV3, DIV3 * 2, DIV3) },
        { fromPixelScale(DIV3, DIV3, 0, DIV3 * 2), fromPixelScale(DIV3, DIV3, DIV3, DIV3 * 2), fromPixelScale(DIV3, DIV3, DIV3 * 2, DIV3 * 2) },
    };
    
    public static final ISubmap[][] X4 = {
        { fromPixelScale(4, 4, 0, 0),  fromPixelScale(4, 4, 4, 0),  fromPixelScale(4, 4, 8, 0),  fromPixelScale(4, 4, 12, 0) },
        { fromPixelScale(4, 4, 0, 4),  fromPixelScale(4, 4, 4, 4),  fromPixelScale(4, 4, 8, 4),  fromPixelScale(4, 4, 12, 4) },
        { fromPixelScale(4, 4, 0, 8),  fromPixelScale(4, 4, 4, 8),  fromPixelScale(4, 4, 8, 8),  fromPixelScale(4, 4, 12, 8) },
        { fromPixelScale(4, 4, 0, 12), fromPixelScale(4, 4, 4, 12), fromPixelScale(4, 4, 8, 12), fromPixelScale(4, 4, 12, 12) },
    };
    
    public static ISubmap[][] grid(int w, int h) {
        float xDiv = 16f / w;
        float yDiv = 16f / h;
        ISubmap[][] ret = new ISubmap[h][w];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                ret[y][x] = fromPixelScale(xDiv, yDiv, xDiv * x, yDiv * y);
            }
        }
        return ret;
    }

    public static ISubmap raw(float width, float height, float xOffset, float yOffset) {
        return new Submap(width, height, xOffset, yOffset, 1);
    }

    public static ISubmap fromUnitScale(float width, float height, float xOffset, float yOffset) {
        return fromPixelScale(width * PIXELS_PER_UNIT, height * PIXELS_PER_UNIT, xOffset * PIXELS_PER_UNIT, yOffset * PIXELS_PER_UNIT);
    }

    public static ISubmap fromPixelScale(float width, float height, float xOffset, float yOffset) {
        return new Submap(width, height, xOffset, yOffset, UNITS_PER_PIXEL);
    }

    @Getter
    private final float width, height;
    @Getter
    private final float xOffset, yOffset;

    final SubmapRescaled rescaled;
    
    private Submap(float width, float height, float xOffset, float yOffset, float rescale) {
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.rescaled = new SubmapRescaled(this, rescale, false);
    }

    @Override
    public SubmapRescaled pixelScale() {
        return this.rescaled;
    }
}