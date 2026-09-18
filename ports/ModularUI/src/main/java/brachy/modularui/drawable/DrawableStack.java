package brachy.modularui.drawable;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.utils.serialization.codec.CodecUtil;

import net.minecraft.util.ExtraCodecs;
import com.mojang.serialization.Codec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A stack of {@link IDrawable} backed by an array which are drawn on top of each other.
 */
public record DrawableStack(IDrawable... drawables) implements IDrawable {

    public static final Codec<IDrawable> CODEC = Codec.lazyInitialized(() ->
            CodecUtil.checkedEncoder(IDrawable.CODEC.listOf().xmap(DrawableStack::fromList, DrawableStack::toList),
                    d -> d instanceof DrawableStack));

    public static final IDrawable[] EMPTY_BACKGROUND = {};
    public static final DrawableStack EMPTY = new DrawableStack(EMPTY_BACKGROUND);

    public static IDrawable fromList(List<IDrawable> list) {
        return IDrawable.of(list.toArray(IDrawable[]::new));
    }

    public static List<IDrawable> toList(IDrawable drawable) {
        if (drawable instanceof DrawableStack(IDrawable[] drawables)) {
            return List.of(drawables);
        }
        return Collections.singletonList(drawable);
    }

    public DrawableStack(IDrawable... drawables) {
        this.drawables = drawables == null || drawables.length == 0 ? EMPTY_BACKGROUND : drawables;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null) drawable.draw(context, x, y, width, height, widgetTheme);
        }
    }

    @Override
    public boolean canApplyTheme() {
        for (IDrawable drawable : this.drawables) {
            if (drawable != null && drawable.canApplyTheme()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof DrawableStack that)) return false;

        return Arrays.equals(this.drawables, that.drawables);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.drawables);
    }
}
