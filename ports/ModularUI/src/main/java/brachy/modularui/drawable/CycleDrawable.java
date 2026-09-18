package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.serialization.codec.MutableObjectCodec;

import com.mojang.serialization.Codec;

import com.google.common.collect.Iterables;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Accessors(fluent = true, chain = true)
public class CycleDrawable implements IDrawable {

    public static final MutableObjectCodec<CycleDrawable> CODEC = MutableObjectCodec.drawableBuilder(CycleDrawable::new)
            .addOpt("drawables", CycleDrawable::drawables, CycleDrawable::getListDrawables, IDrawable.CODEC.listOf(), Collections.emptyList())
            .addOpt("cycleTime", CycleDrawable::cycleTime, CycleDrawable::cycleTime, Codec.INT, 1000)
            .build();

    private IDrawable[] drawables;
    @Getter
    @Setter
    private int cycleTime = 1000;

    public CycleDrawable() {
        this(DrawableStack.EMPTY_BACKGROUND);
    }

    public CycleDrawable(IDrawable... drawables) {
        this.drawables = drawables;
    }

    public CycleDrawable(Iterable<IDrawable> drawables) {
        this(Iterables.toArray(drawables, IDrawable.class));
    }

    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        IDrawable current = getCurrent();
        if (current == null) return;
        current.draw(context, x, y, width, height, widgetTheme);
    }

    public @Nullable IDrawable getCurrent() {
        if (this.drawables.length == 0) return null;
        return this.drawables[Math.abs((int) (System.currentTimeMillis() / this.cycleTime) % this.drawables.length)];
    }

    @Override
    public boolean canApplyTheme() {
        IDrawable current = getCurrent();
        return current != null && current.canApplyTheme();
    }

    public IDrawable[] drawables() {
        return this.drawables;
    }

    public List<IDrawable> getListDrawables() {
        return Arrays.asList(this.drawables);
    }

    public CycleDrawable drawables(IDrawable... drawables) {
        this.drawables = drawables;
        return this;
    }

    public CycleDrawable drawables(Iterable<IDrawable> drawables) {
        return drawables(Iterables.toArray(drawables, IDrawable.class));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        CycleDrawable drawable = (CycleDrawable) o;
        return cycleTime == drawable.cycleTime && Objects.deepEquals(drawables, drawable.drawables);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(drawables), cycleTime);
    }
}
