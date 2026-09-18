package brachy.modularui.drawable.progress;

import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.Rectangle;

import brachy.modularui.utils.math.MathUtils;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.UnaryOperator;

/**
 * A progress drawable which is composed of any number of progress drawable.
 * For each part a progress end and an area transformation has to be supplied.
 */
public class CompositeProgress extends BaseProgressDrawable<CompositeProgress> {

    /**
     * Creates a circle like, 4-slice progress bar. The texture is split into four quadrants which are drawn in either left, right, up
     * or down direction.
     *
     * @param emptyTexture empty texture which is always displayed fully
     * @param fullTexture  the filled texture which will be 4-sliced
     * @param direction    clockwise or counterclockwise direction
     * @return progress drawable
     */
    public static CompositeProgress circularLike4Slice(IDrawable emptyTexture, IDrawable fullTexture, CircularProgressDrawable.Direction direction) {
        CompositeProgress prog = new CompositeProgress().emptyTexture(emptyTexture);
        if (direction == CircularProgressDrawable.Direction.CCW) {
            prog.add(fullTexture, 0, 0, 0.5f, 0.5f, ProgressDrawable.Direction.DOWN, 0.25f)
                    .add(fullTexture, 0, 0.5f, 0.5f, 1, ProgressDrawable.Direction.RIGHT, 0.5f)
                    .add(fullTexture, 0.5f, 0.5f, 1, 1, ProgressDrawable.Direction.UP, 0.75f)
                    .add(fullTexture, 0.5f, 0, 1, 0.5f, ProgressDrawable.Direction.LEFT, 1f);

        } else {
            prog.add(fullTexture, 0, 0, 0.5f, 0.5f, ProgressDrawable.Direction.RIGHT, 0.25f)
                    .add(fullTexture, 0.5f, 0, 1, 0.5f, ProgressDrawable.Direction.DOWN, 0.5f)
                    .add(fullTexture, 0.5f, 0.5f, 1, 1, ProgressDrawable.Direction.LEFT, 0.75f)
                    .add(fullTexture, 0, 0.5f, 0.5f, 1, ProgressDrawable.Direction.UP, 1f);
        }
        return prog;

    }

    @Getter private final List<Part> parts = new ArrayList<>();
    private boolean dirty = true;

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        super.draw(context, x, y, width, height, widgetTheme);
        if (this.dirty) compile();
        float p = getCurrentProgress(width, height);
        Rectangle rect = new Rectangle();
        boolean next = true;
        for (Part part : this.parts) {
            if (!next) break;
            rect.set(x, y, width, height);
            part.area.apply(rect);
            part.progress.draw(context, rect.x, rect.y, rect.width, rect.height, widgetTheme);
            next = p > part.until;
        }
    }

    private void compile() {
        this.dirty = false;
        this.parts.sort(Part::compareTo);
        float last = 0;
        for (Part part : this.parts) {
            final float m = 1 / (part.until - last);
            float finalLast = last;
            part.progress.progress(() -> Mth.clamp((getCurrentProgress(1, 1) - finalLast) * m, 0, 1));
            last = part.until;
        }
    }

    public CompositeProgress add(Part part) {
        this.parts.add(part);
        this.dirty = true;
        return this;
    }

    public CompositeProgress add(BaseProgressDrawable<?> progress, float until, UnaryOperator<Rectangle> area) {
        return add(new Part(progress, until, area));
    }

    public CompositeProgress add(IDrawable filledTexture, float u0, float v0, float u1, float v1, ProgressDrawable.Direction direction, float untilProgress) {
        return add(filledTexture, u0, v0, u1, v1, direction, untilProgress, 0);
    }

    public CompositeProgress add(IDrawable filledTexture, float u0, float v0, float u1, float v1, ProgressDrawable.Direction direction, float untilProgress, int progressPixelStep) {
        return add(new ProgressDrawable()
                        .direction(direction)
                        .filledTexture(filledTexture.getSubArea(u0, v0, u1, v1))
                        .progressPixelStepSize(progressPixelStep),
                untilProgress,
                r -> {
                    r.subArea(u0, v0, u1, v1);
                    return r;
                });
    }

    @Override
    public CompositeProgress progress(DoubleSupplier progress) {
        this.dirty = true;
        return super.progress(progress);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompositeProgress that)) return false;
        if (!super.equals(o)) return false;

        return this.parts.equals(that.parts);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.parts.hashCode();
        return result;
    }

    public static class Part implements Comparable<Part> {

        private final BaseProgressDrawable<?> progress;
        private final float until;
        private final UnaryOperator<Rectangle> area;

        public Part(BaseProgressDrawable<?> progress, float until, UnaryOperator<Rectangle> area) {
            this.progress = progress;
            this.until = until;
            this.area = area;
        }

        @Override
        public int compareTo(@NotNull CompositeProgress.Part o) {
            return Float.compare(this.until, o.until);
        }
    }
}
