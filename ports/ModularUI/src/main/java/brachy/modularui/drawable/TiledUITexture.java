package brachy.modularui.drawable;

import brachy.modularui.screen.viewport.GuiContext;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.systems.RenderSystem;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.Objects;

@Accessors(fluent = true)
public class TiledUITexture extends UITexture {

    @Getter private final int imageWidth, imageHeight;

    /**
     * Use {@link UITexture#builder()} with {@link Builder#tiled()}
     */
    TiledUITexture(Identifier location, float u0, float v0, float u1, float v1, ColorType colorType,
                   boolean nonOpaque, int colorOverride, int imageWidth, int imageHeight) {
        super(location, u0, v0, u1, v1, colorType, nonOpaque, colorOverride);
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @Override
    public TiledUITexture register(String name) {
        return (TiledUITexture) super.register(name);
    }

    @Override
    public void draw(GuiContext context, float x, float y, float width, float height) {
        if (width == this.imageWidth && height == this.imageHeight) {
            super.draw(context, x, y, width, height);
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        GuiDraw.drawTiledTexture(context.getLastGraphicsPose(), this.location, x, y, width, height,
                this.u0, this.v0, this.u1, this.v1,
                this.imageWidth, this.imageHeight, 0);
    }

    @Override
    protected TiledUITexture copy() {
        return new TiledUITexture(location, u0, v0, u1, v1, colorType, nonOpaque, colorOverride, imageWidth, imageHeight);
    }

    @Override
    public TiledUITexture withColorOverride(int color) {
        return (TiledUITexture) super.withColorOverride(color);
    }

    @Override
    public Builder toBuilder() {
        return super.toBuilder().tiled(this.imageWidth, this.imageHeight);
    }

    @Override
    public boolean equals(Object o) {
        return o != null && getClass() == o.getClass() && isEqual((TiledUITexture) o);
    }

    protected boolean isEqual(TiledUITexture texture) {
        return super.isEqual(texture) && imageWidth == texture.imageWidth && imageHeight == texture.imageHeight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), imageWidth, imageHeight);
    }
}
