package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.DrawableSerialization;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;

import lombok.Getter;

/**
 * A json builder with helper methods to make building themes in java easier.
 * This class is meant to be extended for custom helper methods.
 *
 * @param <B> type of this builder class
 */
public class ThemeBuilder<B extends ThemeBuilder<B>> extends JsonBuilder {

    @Getter
    private final String id;
    @Getter
    private String parent;

    public ThemeBuilder(String id) {
        this.id = id;
    }

    protected B getThis() {
        return (B) this;
    }

    public B parent(String v) {
        add(IThemeApi.PARENT, v);
        this.parent = v;
        return getThis();
    }

    public B defaultBackground(IDrawable v) {
        add(IThemeApi.BACKGROUND, DrawableSerialization.serialize(v));
        return getThis();
    }

    public B defaultBackground(String textureId) {
        add(IThemeApi.BACKGROUND, new JsonBuilder().add("type", "texture").add("id", textureId));
        return getThis();
    }

    public B defaultHoverBackground(IDrawable v) {
        mergeAdd(IThemeApi.HOVER_SUFFIX,
                new JsonBuilder().add(IThemeApi.BACKGROUND, DrawableSerialization.serialize(v)));
        return getThis();
    }

    public B defaultHoverBackground(String textureId) {
        mergeAdd(IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.BACKGROUND,
                new JsonBuilder().add("type", "texture").add("id", textureId)));
        return getThis();
    }

    public B defaultColor(int v) {
        add(IThemeApi.COLOR, v);
        return getThis();
    }

    public B defaultHoverColor(int v) {
        mergeAdd(IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.COLOR, v));
        return getThis();
    }

    public B defaultTextColor(int v) {
        add(IThemeApi.TEXT_COLOR, v);
        return getThis();
    }

    public B defaultTextHoverColor(int v) {
        mergeAdd(IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.TEXT_COLOR, v));
        return getThis();
    }

    public B defaultTextShadow(boolean v) {
        add(IThemeApi.TEXT_SHADOW, v);
        return getThis();
    }

    public B defaultTextHoverShadow(boolean v) {
        mergeAdd(IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.TEXT_SHADOW, v));
        return getThis();
    }

    public B defaultIconColor(int v) {
        add(IThemeApi.ICON_COLOR, v);
        return getThis();
    }

    public B defaultIconHoverColor(int v) {
        mergeAdd(IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.ICON_COLOR, v));
        return getThis();
    }

    public B defaultWidth(int v) {
        add(IThemeApi.ICON_COLOR, v);
        return getThis();
    }

    public B defaultHeight(int v) {
        mergeAdd(IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.ICON_COLOR, v));
        return getThis();
    }

    public B background(WidgetThemeKey<?> widgetTheme, IDrawable v) {
        mergeAdd(widgetTheme.getFullName(),
                new JsonBuilder().add(IThemeApi.BACKGROUND, DrawableSerialization.serialize(v)));
        return getThis();
    }

    public B background(WidgetThemeKey<?> widgetTheme, String textureId) {
        return background(widgetTheme, new JsonBuilder().add("type", "texture").add("id", textureId));
    }

    public B background(WidgetThemeKey<?> widgetTheme, JsonBuilder builder) {
        mergeAdd(widgetTheme.getFullName(),
                new JsonBuilder().add(IThemeApi.BACKGROUND, builder));
        return getThis();
    }

    public B hoverBackground(WidgetThemeKey<?> widgetTheme, IDrawable v) {
        mergeAdd(widgetTheme.getFullName() + IThemeApi.HOVER_SUFFIX,
                new JsonBuilder().add(IThemeApi.BACKGROUND, DrawableSerialization.serialize(v)));
        return getThis();
    }

    public B hoverBackground(WidgetThemeKey<?> widgetTheme, String textureId) {
        return hoverBackground(widgetTheme, new JsonBuilder().add("type", "texture").add("id", textureId));
    }

    public B hoverBackground(WidgetThemeKey<?> widgetTheme, JsonBuilder builder) {
        mergeAdd(widgetTheme.getFullName() + IThemeApi.HOVER_SUFFIX,
                new JsonBuilder().add(IThemeApi.BACKGROUND, builder));
        return getThis();
    }

    public B color(WidgetThemeKey<?> widgetTheme, int v) {
        mergeAdd(widgetTheme.getFullName(), new JsonBuilder().add(IThemeApi.COLOR, v));
        return getThis();
    }

    public B hoverColor(WidgetThemeKey<?> widgetTheme, int v) {
        mergeAdd(widgetTheme.getFullName() + IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.COLOR, v));
        return getThis();
    }

    public B textColor(WidgetThemeKey<?> widgetTheme, int v) {
        mergeAdd(widgetTheme.getFullName(), new JsonBuilder().add(IThemeApi.TEXT_COLOR, v));
        return getThis();
    }

    public B textHoverColor(WidgetThemeKey<?> widgetTheme, int v) {
        mergeAdd(widgetTheme.getFullName() + IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.TEXT_COLOR, v));
        return getThis();
    }

    public B textShadow(WidgetThemeKey<?> widgetTheme, boolean v) {
        mergeAdd(widgetTheme.getFullName(), new JsonBuilder().add(IThemeApi.TEXT_SHADOW, v));
        return getThis();
    }

    public B textHoverShadow(WidgetThemeKey<?> widgetTheme, boolean v) {
        mergeAdd(widgetTheme.getFullName() + IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.TEXT_SHADOW, v));
        return getThis();
    }

    public B iconColor(WidgetThemeKey<?> widgetTheme, int v) {
        mergeAdd(widgetTheme.getFullName(), new JsonBuilder().add(IThemeApi.ICON_COLOR, v));
        return getThis();
    }

    public B iconHoverColor(WidgetThemeKey<?> widgetTheme, int v) {
        mergeAdd(widgetTheme.getFullName() + IThemeApi.HOVER_SUFFIX, new JsonBuilder().add(IThemeApi.ICON_COLOR, v));
        return getThis();
    }

    public B itemSlotHoverColor(int v) {
        mergeAdd(IThemeApi.ITEM_SLOT.getFullName(), new JsonBuilder().add(IThemeApi.SLOT_HOVER_COLOR, v));
        return getThis();
    }

    public B fluidSlotHoverColor(int v) {
        mergeAdd(IThemeApi.FLUID_SLOT.getName(), new JsonBuilder().add(IThemeApi.SLOT_HOVER_COLOR, v));
        return getThis();
    }

    public B textFieldMarkedColor(int v) {
        mergeAdd(IThemeApi.TEXT_FIELD.getName(), new JsonBuilder().add(IThemeApi.MARKED_COLOR, v));
        return getThis();
    }

    public B textFieldHintColor(int v) {
        mergeAdd(IThemeApi.TEXT_FIELD.getName(), new JsonBuilder().add(IThemeApi.HINT_COLOR, v));
        return getThis();
    }

    /**
     * Customizes widget themes in a more organized way than with the methods above.
     *
     * @param widgetThemeKey     key of the widget theme to customize
     * @param widgetThemeBuilder builder of the widget theme (take a look at its subclasses)
     * @param <T>                type of the widget theme
     * @return this
     */
    public <T extends WidgetTheme> B widgetTheme(WidgetThemeKey<T> widgetThemeKey,
                                                 WidgetThemeBuilder<T, ?> widgetThemeBuilder) {
        add(widgetThemeKey.getFullName(), widgetThemeBuilder);
        return getThis();
    }

    /**
     * Customizes widget themes in a more organized way than with the methods above.
     *
     * @param widgetThemeKey     key of the widget theme to customize
     * @param widgetThemeBuilder builder of the widget theme (take a look at its subclasses)
     * @param <T>                type of the widget theme
     * @return this
     */
    public <T extends WidgetTheme> B widgetThemeHover(WidgetThemeKey<T> widgetThemeKey,
                                                      WidgetThemeBuilder<T, ?> widgetThemeBuilder) {
        add(widgetThemeKey.getFullName() + IThemeApi.HOVER_SUFFIX, widgetThemeBuilder);
        return getThis();
    }
}
