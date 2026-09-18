package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widget.Widget;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class SpriteDrawable implements IDrawable {

    private final TextureAtlasSprite sprite;
    private boolean canApplyTheme = false;

    public SpriteDrawable(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        GuiDraw.drawSprite(context.getLastGraphicsPose(), this.sprite, x, y, width, height);
    }

    @Override
    public Widget<?> asWidget() {
        return IDrawable.super.asWidget().size(this.sprite.contents().width(), this.sprite.contents().height());
    }

    @Override
    public Icon asIcon() {
        return IDrawable.super.asIcon().size(this.sprite.contents().width(), this.sprite.contents().height());
    }

    public SpriteDrawable canApplyTheme(boolean canApplyTheme) {
        this.canApplyTheme = canApplyTheme;
        return this;
    }

    @Override
    public boolean canApplyTheme() {
        return canApplyTheme;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof SpriteDrawable that)) return false;

        return canApplyTheme == that.canApplyTheme &&
                sprite.atlasLocation().equals(that.sprite.atlasLocation()) &&
                sprite.contents().name().equals(that.sprite.contents().name());
    }

    @Override
    public int hashCode() {
        int result = sprite.hashCode();
        result = 31 * result + Boolean.hashCode(canApplyTheme);
        return result;
    }
}
