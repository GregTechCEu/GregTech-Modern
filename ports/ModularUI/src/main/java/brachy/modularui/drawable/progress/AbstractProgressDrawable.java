package brachy.modularui.drawable.progress;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

import lombok.Getter;

import java.util.Objects;

public abstract class AbstractProgressDrawable<D extends AbstractProgressDrawable<D>> extends BaseProgressDrawable<D> {

    @Getter private IDrawable filledTexture;

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        super.draw(context, x, y, width, height, widgetTheme);
        if (this.filledTexture == null) return;
        float p = getCurrentProgress(width, height);
        if (p == 0f) return;
        if (p < 1f) pushProgressStencil(p, context, x, y, width, height, widgetTheme);
        getFilledTexture().draw(context, x, y, width, height, widgetTheme);
        if (p < 1f) context.getStencil().pop();
    }

    protected abstract void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme);

    /**
     * Sets the filled texture which is partially drawn based on the current progress.
     *
     * @param drawable filled texture.
     * @return this
     */
    public D filledTexture(IDrawable drawable) {
        this.filledTexture = drawable;
        return self();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractProgressDrawable<?> that)) return false;
        if (!super.equals(o)) return false;

        return Objects.equals(filledTexture, that.filledTexture);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Objects.hashCode(filledTexture);
        return result;
    }
}
