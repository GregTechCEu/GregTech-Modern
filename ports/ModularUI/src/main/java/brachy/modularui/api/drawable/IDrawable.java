package brachy.modularui.api.drawable;

import brachy.modularui.api.widget.IWidget;
import brachy.modularui.drawable.DrawableStack;
import brachy.modularui.drawable.Icon;
import brachy.modularui.drawable.SubAreaDrawable;
import brachy.modularui.screen.viewport.GuiContext;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.serialization.codec.CodecRegistry;
import brachy.modularui.utils.serialization.codec.CodecUtil;
import brachy.modularui.widget.Widget;
import brachy.modularui.widget.sizer.Area;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.Nullable;

/**
 * An object which can be drawn at any size. This is mainly used for backgrounds and overlays in
 * {@link IWidget}.
 * To draw at a fixed size, use {@link IIcon} (see {@link #asIcon()}).
 */
public interface IDrawable {

    static IDrawable of(IDrawable... drawables) {
        if (drawables == null || drawables.length == 0) {
            return null;
        } else if (drawables.length == 1) {
            return drawables[0];
        } else {
            return new DrawableStack(drawables);
        }
    }

    /**
     * An empty drawable. Does nothing.
     */
    IDrawable EMPTY = new IDrawable() {
        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {}

        @Override
        public String toString() {
            return "IDrawable.EMPTY";
        }
    };

    /**
     * An empty drawable used to mark hover textures as "should not be used"!
     */
    IDrawable NONE = new IDrawable() {
        @Override
        public void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme) {}

        @Override
        public String toString() {
            return "IDrawable.NONE";
        }
    };

    CodecRegistry<IDrawable> CODECS = new CodecRegistry<>();
    MapCodec<IDrawable> CODEC_DISPATCH = CodecUtil.dispatchNullable(Codec.STRING, IDrawable::getTypeName, CODECS::getNullable);
    Codec<IDrawable> CODEC_EMPTY_NONE = Codec.STRING.flatXmap(s -> {
        if (s == null || s.equals("empty") || s.equals("null")) return DataResult.success(EMPTY);
        if (s.equals("none")) return DataResult.success(NONE);
        return DataResult.error(() -> "Only valid options are empty, null and none");
    }, d -> {
        if (d == EMPTY) return DataResult.success("empty");
        if (d == NONE) return DataResult.success("none");
        return DataResult.error(() -> "Only works for empty and none");
    });
    Codec<IDrawable> CODEC = CodecUtil.chainedCodec(
            CodecUtil.nullCodec(EMPTY), CODEC_EMPTY_NONE,
            DrawableStack.CODEC, CODEC_DISPATCH.codec());

    static DataResult<JsonElement> toJson(IDrawable drawable) {
        return CODEC.encodeStart(JsonOps.INSTANCE, drawable);
    }

    static JsonElement toJsonOrThrow(IDrawable drawable) {
        return toJson(drawable).getOrThrow();
    }

    /**
     * Draws this drawable at the given position with the given size. It's the implementors responsibility to properly
     * apply the widget theme by calling {@link #applyColor(int)} before drawing.
     *
     * @param context     current context to draw with
     * @param x           x position
     * @param y           y position
     * @param width       draw width
     * @param height      draw height
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    void draw(GuiContext context, int x, int y, int width, int height, WidgetTheme widgetTheme);

    /**
     * Draws this drawable at the current (0|0) with the given size. This is useful inside widgets since GL is
     * transformed to their position when they are drawing.
     *
     * @param context     gui context
     * @param width       draw width
     * @param height      draw height
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void drawAtZero(GuiContext context, int width, int height, WidgetTheme widgetTheme) {
        draw(context, 0, 0, width, height, widgetTheme);
    }

    /**
     * Draws this drawable in a given area. The padding of the area is not applied here.
     *
     * @param context     current context to draw with
     * @param area        draw area
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void draw(GuiContext context, Area area, WidgetTheme widgetTheme) {
        draw(context, area.x, area.y, area.width,
                area.height, widgetTheme);
    }

    /**
     * Draws this drawable in a given area with its padding applied.
     *
     * @param context     current context to draw with
     * @param area        draw area
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void drawPadded(GuiContext context, Area area, WidgetTheme widgetTheme) {
        draw(context, area.x + area.getPadding().left(), area.y + area.getPadding().top(),
                area.paddedWidth(), area.paddedHeight(), widgetTheme);
    }

    /**
     * Draws this drawable at the current (0|0) with the given area's size. This is useful inside widgets since GL is
     * transformed to their position when they are drawing. The padding of the area is not applied here.
     *
     * @param context     gui context
     * @param area        draw area
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void drawAtZero(GuiContext context, Area area, WidgetTheme widgetTheme) {
        draw(context, 0, 0, area.width, area.height, widgetTheme);
    }

    /**
     * Draws this drawable at the current (0|0) with the given area's size and its padding applied
     * (this means its technically not at 0|0). This is useful inside widgets since GL is transformed to their position
     * when they are drawing.
     *
     * @param context     gui context
     * @param area        draw area
     * @param widgetTheme current theme
     */
    @OnlyIn(Dist.CLIENT)
    default void drawAtZeroPadded(GuiContext context, Area area, WidgetTheme widgetTheme) {
        draw(context, area.getPadding().left(), area.getPadding().top(), area.paddedWidth(), area.paddedHeight(), widgetTheme);
    }

    /**
     * @return if theme color can be applied on this drawable
     */
    default boolean canApplyTheme() {
        return false;
    }

    /**
     * Applies the theme color to OpenGL if this drawable can have theme colors applied. This is determined by
     * {@link #canApplyTheme()}.
     * If this drawable does not allow theme colors, it will reset the current color (to white).
     * This method should be called before drawing.
     *
     * @param themeColor theme color to apply (usually {@link WidgetTheme#getColor()})
     */
    default void applyColor(int themeColor) {
        if (canApplyTheme()) {
            Color.setGlColor(themeColor);
        } else {
            Color.setGlColorOpaque(Color.WHITE.main);
        }
    }

    default int getDefaultWidth() {
        return 18;
    }

    default int getDefaultHeight() {
        return 18;
    }

    /**
     * @return a widget with this drawable as a background
     */
    default Widget<?> asWidget() {
        return new DrawableWidget(this);
    }

    /**
     * @return this drawable as an icon
     */
    default Icon asIcon() {
        return new Icon(this).size(getDefaultWidth(), getDefaultHeight());
    }

    default IDrawable getSubArea(float u0, float v0, float u1, float v1) {
        return new SubAreaDrawable(this).uv(u0, v0, u1, v1);
    }

    default String getTypeName() {
        return getClass().getSimpleName();
    }

    static boolean isVisible(@Nullable IDrawable drawable) {
        if (drawable == null || drawable == EMPTY || drawable == NONE) return false;
        if (drawable instanceof DrawableStack array) {
            return array.drawables().length > 0;
        }
        return true;
    }

    /**
     * A widget wrapping a drawable. The drawable is drawn between the background and the overlay.
     */
    class DrawableWidget extends Widget<DrawableWidget> {

        private final IDrawable drawable;

        public DrawableWidget(IDrawable drawable) {
            this.drawable = drawable;
        }

        @OnlyIn(Dist.CLIENT)
        @Override
        public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
            this.drawable.drawAtZero(context, getArea(), getActiveWidgetTheme(widgetTheme, isHovering()));
        }
    }
}
