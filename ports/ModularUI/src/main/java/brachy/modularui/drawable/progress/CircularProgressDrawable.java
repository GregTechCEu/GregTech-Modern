package brachy.modularui.drawable.progress;

import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;

import brachy.modularui.drawable.GuiShapeRenderState;
import lombok.Getter;

/**
 * A progress texture which translates the progress into a circular angle. This works with any {@link brachy.modularui.api.drawable.IDrawable}.
 */
public class CircularProgressDrawable extends AbstractProgressDrawable<CircularProgressDrawable> {

    @Getter private Direction direction = Direction.CW;

    @Override
    protected void pushProgressStencil(float progress, GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        context.getStencil().push(() -> GuiShapeRenderState.submit(context.getGraphics(),
                RadialMask.vertices(progress, x, y, width, height, direction == Direction.CW),
                GuiShapeRenderState.Topology.TRIANGLE_FAN), x, y, width, height);
    }

    public CircularProgressDrawable direction(Direction direction) {
        this.direction = direction == null ? Direction.CW : direction;
        return this;
    }

    public CircularProgressDrawable clockwise() {
        return direction(Direction.CW);
    }

    public CircularProgressDrawable counterClockwise() {
        return direction(Direction.CCW);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CircularProgressDrawable that)) return false;
        if (!super.equals(o)) return false;

        return direction == that.direction;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + direction.hashCode();
        return result;
    }

    public enum Direction {
        CW, CCW
    }
}
