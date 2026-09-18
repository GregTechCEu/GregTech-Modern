package brachy.modularui.widgets;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.value.IDoubleValue;
import brachy.modularui.api.value.ISyncOrValue;
import brachy.modularui.drawable.progress.BaseProgressDrawable;
import brachy.modularui.drawable.progress.ProgressDrawable;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.value.DoubleValue;
import brachy.modularui.widget.Widget;

import org.jetbrains.annotations.NotNull;

import java.util.function.DoubleSupplier;

public class ProgressWidget extends Widget<ProgressWidget> {

    private BaseProgressDrawable<?> progress;
    private IDoubleValue<?> value;

    public ProgressWidget() {}

    public ProgressWidget(BaseProgressDrawable<?> progress) {
        this.progress = progress;
    }

    @Override
    public boolean isValidSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        return syncOrValue.isTypeOrEmpty(IDoubleValue.class);
    }

    @Override
    protected void setSyncOrValue(@NotNull ISyncOrValue syncOrValue) {
        super.setSyncOrValue(syncOrValue);
        this.value = syncOrValue.castNullable(IDoubleValue.class);
        if (this.value != null && this.progress != null) {
            this.progress.progress(this.value::getDoubleValue);
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        if (this.progress != null) {
            this.progress.drawAtZeroPadded(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }

    public ProgressWidget progress(BaseProgressDrawable<?> progress) {
        if (this.progress != null) {
            this.progress.progress(null); // remove potential reference to the IDoubleValue
        }
        this.progress = progress;
        if (this.value != null && this.progress != null) {
            this.progress.progress(this.value::getDoubleValue);
        }
        return this;
    }

    public ProgressWidget value(IDoubleValue<?> doubleValue) {
        setSyncOrValue(ISyncOrValue.orEmpty(doubleValue));
        return this;
    }

    public ProgressWidget clientValue(DoubleSupplier doubleValue) {
        return value(new DoubleValue.Dynamic(doubleValue, null));
    }

    /**
     * Sets the texture to render
     *
     * @param emptyTexture empty bar, always rendered
     * @param fullTexture  full bar, partly rendered, based on progress
     */
    public ProgressWidget texture(IDrawable emptyTexture, IDrawable fullTexture, ProgressDrawable.Direction direction) {
        return progress(new ProgressDrawable()
                .emptyTexture(emptyTexture)
                .filledTexture(fullTexture)
                .direction(direction));
    }

    /**
     * @param texture a texture where the empty and full bar are stacked on top of each other
     */
    public ProgressWidget texture(IDrawable texture, ProgressDrawable.Direction direction) {
        return texture(texture.getSubArea(0, 0, 1, 0.5f), texture.getSubArea(0, 0.5f, 1, 1), direction);
    }
}
