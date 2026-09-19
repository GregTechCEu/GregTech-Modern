package brachy.modularui.drawable;

import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.resources.Identifier;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

/**
 * This class is a <a href="https://en.wikipedia.org/wiki/9-slice_scaling">9-slice texture</a>. It can be created using
 * {@link UITexture.Builder#adaptable(int, int, int, int)}.
 */
@Accessors(fluent = true)
public class AdaptableUITexture extends UITexture {

    @Getter private final int imageWidth, imageHeight, bl, bt, br, bb;
    @Getter private final boolean tiled;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#adaptable(int, int)}
     */
    AdaptableUITexture(Identifier location, float u0, float v0, float u1, float v1, ColorType colorType, boolean nonOpaque,
                       int colorOverride, int imageWidth, int imageHeight, int bl, int bt, int br, int bb, boolean tiled) {
        super(location, u0, v0, u1, v1, colorType, nonOpaque, colorOverride);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.bl = bl;
        this.bt = bt;
        this.br = br;
        this.bb = bb;
        this.tiled = tiled;
    }

    @Override
    public AdaptableUITexture register(String name) {
        return (AdaptableUITexture) super.register(name);
    }

    @Override
    public AdaptableUITexture getSubArea(float uStart, float vStart, float uEnd, float vEnd) {
        return new AdaptableUITexture(this.location, lerpU(uStart), lerpV(vStart), lerpU(uEnd), lerpV(vEnd),
                this.colorType, this.nonOpaque, this.colorOverride, this.imageWidth, this.imageHeight, this.bl, this.bt, this.br, this.bb,
                this.tiled);
    }

    @Override
    public void draw(GuiContext context, float x, float y, float width, float height) {
        if (width == this.imageWidth && height == this.imageHeight) {
            super.draw(context, x, y, width, height);
            return;
        }
        if (this.tiled) {
            drawTiled(context, x, y, width, height);
        } else {
            drawStretched(context, x, y, width, height);
        }
    }

    public void drawStretched(GuiContext context, float x, float y, float width, float height) {
        drawSlices(context, x, y, width, height, false);
    }

    public void drawTiled(GuiContext context, float x, float y, float width, float height) {
        drawSlices(context, x, y, width, height, true);
    }

    private void drawSlices(GuiContext context, float x, float y, float width, float height, boolean tile) {
        if (width <= 0 || height <= 0) return;
        float horizontalScale = this.bl + this.br == 0 ? 1 : Math.min(1, width / (this.bl + this.br));
        float verticalScale = this.bt + this.bb == 0 ? 1 : Math.min(1, height / (this.bt + this.bb));
        float[] xs = {x, x + this.bl * horizontalScale, x + width - this.br * horizontalScale, x + width};
        float[] ys = {y, y + this.bt * verticalScale, y + height - this.bb * verticalScale, y + height};
        float[] us = {this.u0, this.u0 + (float) this.bl / this.imageWidth,
                this.u1 - (float) this.br / this.imageWidth, this.u1};
        float[] vs = {this.v0, this.v0 + (float) this.bt / this.imageHeight,
                this.v1 - (float) this.bb / this.imageHeight, this.v1};
        var graphics = context.getGraphics();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                float sliceWidth = xs[column + 1] - xs[column];
                float sliceHeight = ys[row + 1] - ys[row];
                if (sliceWidth <= 0 || sliceHeight <= 0) continue;
                if (tile) {
                    int tileWidth = Math.max(1, Math.round((us[column + 1] - us[column]) * this.imageWidth));
                    int tileHeight = Math.max(1, Math.round((vs[row + 1] - vs[row]) * this.imageHeight));
                    GuiDraw.drawTiledTexture(graphics, this.location, xs[column], ys[row], sliceWidth, sliceHeight,
                            us[column], vs[row], us[column + 1], vs[row + 1], tileWidth, tileHeight, this.nonOpaque);
                } else {
                    GuiDraw.drawTexture(graphics, this.location, xs[column], ys[row], xs[column + 1], ys[row + 1],
                            us[column], vs[row], us[column + 1], vs[row + 1], this.nonOpaque);
                }
            }
        }
    }

    @Override
    protected AdaptableUITexture copy() {
        return new AdaptableUITexture(location, u0, v0, u1, v1, colorType, nonOpaque,
                colorOverride, imageWidth, imageHeight, bl, bt, br, bb, tiled);
    }

    @Override
    public AdaptableUITexture withColorOverride(int color) {
        return (AdaptableUITexture) super.withColorOverride(color);
    }

    @Override
    public Builder toBuilder() {
        return super.toBuilder()
                .imageSize(this.imageWidth, this.imageHeight)
                .adaptable(this.bl, this.bt, this.br, this.bb)
                .tiled();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass() && isEqual((AdaptableUITexture) o);
    }

    protected boolean isEqual(AdaptableUITexture texture) {
        return super.isEqual(texture) && imageWidth == texture.imageWidth && imageHeight == texture.imageHeight &&
                bl == texture.bl && bt == texture.bt && br == texture.br && bb == texture.bb && tiled == texture.tiled;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), imageWidth, imageHeight, bl, bt, br, bb, tiled);
    }
}
