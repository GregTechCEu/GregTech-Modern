package brachy.modularui.drawable.progress;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.widgets.ProgressWidget;

import net.minecraft.util.Util;
import net.minecraft.util.Mth;

import lombok.Getter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleSupplier;

public abstract class BaseProgressDrawable<D extends BaseProgressDrawable<D>> implements IDrawable {

    @Getter private IDrawable emptyBackground;
    @Getter private DoubleSupplier progress;
    @Getter protected float progressStepSize = 0;

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        if (this.emptyBackground != null) this.emptyBackground.draw(context, x, y, width, height, widgetTheme);
    }

    protected float getCurrentProgress(int width, int height) {
        float p = this.progress == null ? 1f : (float) this.progress.getAsDouble();
        p = Mth.clamp(p, 0, 1);
        float stepSize = getCurrentProgressStepSize(width, height);
        if (stepSize > 0) {
            int c = (int) (p / stepSize);
            p = c * stepSize;
        }
        return p;
    }

    protected float getCurrentProgressStepSize(int width, int height) {
        return this.progressStepSize;
    }

    /**
     * Sets the displayed progress value. The progress is clamped between 0 (empty) and 1 (filled).
     *
     * @param progress progress supplier
     * @return this
     */
    public D progress(DoubleSupplier progress) {
        this.progress = progress;
        return self();
    }

    /**
     * Sets a fixed progress value to display. The progress is clamped between 0 (empty) and 1 (filled).
     *
     * @param progress progress
     * @return this
     */
    public D progress(double progress) {
        return progress(() -> progress);
    }

    /**
     * Sets a progress supplier which linearly increases from 0 to 1 with the given duration in milliseconds.
     *
     * @param durationMilliSeconds duration in milliseconds
     * @return this
     */
    public D progressDuration(int durationMilliSeconds) {
        return progress(() -> Util.getMillis() % durationMilliSeconds / (double) durationMilliSeconds);
    }

    /**
     * Sets a progress supplier which linearly increases from 0 to 1 with the given duration.
     *
     * @param duration duration
     * @param unit     time unit of the previous duration argument
     * @return this
     */
    public D progressDuration(long duration, TimeUnit unit) {
        return progressDuration((int) unit.toMillis(duration));
    }

    /**
     * Sets a progress step size. The displayed progress will be clamped to the closest multiple of this value.
     * Small values are smooth and high values are choppy. Values higher than 1 means the displayed progress is always 0.
     *
     * @param progressStepSize progress step size
     * @return this.
     */
    public D progressStepSize(float progressStepSize) {
        this.progressStepSize = progressStepSize;
        return self();
    }

    public D smooth() {
        return progressStepSize(-1);
    }

    /**
     * Sets the empty texture which is always fully displayed.
     *
     * @param drawable empty texture
     * @return this
     */
    public D emptyTexture(IDrawable drawable) {
        this.emptyBackground = drawable;
        return self();
    }


    @SuppressWarnings("unchecked")
    protected D self() {
        return (D) this;
    }

    @Override
    public ProgressWidget asWidget() {
        ProgressWidget widget = new ProgressWidget(this);
        if (this.progress != null) widget.clientValue(this.progress);
        return widget;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseProgressDrawable<?> that)) return false;

        return Float.compare(progressStepSize, that.progressStepSize) == 0 &&
                Objects.equals(emptyBackground, that.emptyBackground) &&
                Objects.equals(progress, that.progress);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(emptyBackground);
        result = 31 * result + Objects.hashCode(progress);
        result = 31 * result + Float.hashCode(progressStepSize);
        return result;
    }
}
