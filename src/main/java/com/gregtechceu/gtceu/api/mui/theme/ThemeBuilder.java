package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.DrawableSerialization;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;

public class ThemeBuilder<B extends ThemeBuilder<B>> extends JsonBuilder {

    protected B getThis() {
        return (B) this;
    }

    public B parent(String v) {
        add("parent", v);
        return getThis();
    }

    public B defaultBackground(IDrawable v) {
        add("background", DrawableSerialization.serialize(v));
        return getThis();
    }

    public B defaultBackground(String textureId) {
        add("background", new JsonBuilder().add("type", "texture").add("id", textureId));
        return getThis();
    }

    public B defaultHoverBackground(IDrawable v) {
        add("hoverBackground", DrawableSerialization.serialize(v));
        return getThis();
    }

    public B defaultHoverBackground(String textureId) {
        add("hoverBackground", new JsonBuilder().add("type", "texture").add("id", textureId));
        return getThis();
    }

    public B defaultColor(int v) {
        add("color", v);
        return getThis();
    }

    public B defaultTextColor(int v) {
        add("textColor", v);
        return getThis();
    }

    public B defaultTextShadow(boolean v) {
        add("textShadow", v);
        return getThis();
    }

    public B background(String widgetTheme, IDrawable v) {
        mergeAdd(widgetTheme, new JsonBuilder().add("background", DrawableSerialization.serialize(v)));
        return getThis();
    }

    public B background(String widgetTheme, String textureId) {
        mergeAdd(widgetTheme,
                new JsonBuilder().add("background", new JsonBuilder().add("type", "texture").add("id", textureId)));
        return getThis();
    }

    public B hoverBackground(String widgetTheme, IDrawable v) {
        mergeAdd(widgetTheme, new JsonBuilder().add("hoverBackground", DrawableSerialization.serialize(v)));
        return getThis();
    }

    public B hoverBackground(String widgetTheme, String textureId) {
        mergeAdd(widgetTheme, new JsonBuilder().add("hoverBackground",
                new JsonBuilder().add("type", "texture").add("id", textureId)));
        return getThis();
    }

    public B color(String widgetTheme, int v) {
        mergeAdd(widgetTheme, new JsonBuilder().add("color", v));
        return getThis();
    }

    public B textColor(String widgetTheme, int v) {
        mergeAdd(widgetTheme, new JsonBuilder().add("textColor", v));
        return getThis();
    }

    public B textShadow(String widgetTheme, boolean v) {
        mergeAdd(widgetTheme, new JsonBuilder().add("textShadow", v));
        return getThis();
    }

    public B itemSlotHoverColor(int v) {
        mergeAdd(IThemeApi.ITEM_SLOT, new JsonBuilder().add("slotHoverColor", v));
        return getThis();
    }

    public B fluidSlotHoverColor(int v) {
        mergeAdd(IThemeApi.FLUID_SLOT, new JsonBuilder().add("slotHoverColor", v));
        return getThis();
    }

    public B textFieldMarkedColor(int v) {
        mergeAdd(IThemeApi.TEXT_FIELD, new JsonBuilder().add("markedColor", v));
        return getThis();
    }

    public B textFieldHintColor(int v) {
        mergeAdd(IThemeApi.TEXT_FIELD, new JsonBuilder().add("hintColor", v));
        return getThis();
    }

    public B toggleButtonSelectedBackground(IDrawable v) {
        mergeAdd(IThemeApi.TOGGLE_BUTTON,
                new JsonBuilder().add("selectedBackground", DrawableSerialization.serialize(v)));
        return getThis();
    }

    public B toggleButtonSelectedBackground(String widgetTheme, String textureId) {
        mergeAdd(IThemeApi.TOGGLE_BUTTON, new JsonBuilder().add("selectedBackground",
                new JsonBuilder().add("type", "texture").add("id", textureId)));
        return getThis();
    }

    public B toggleButtonSelectedHoverBackground(IDrawable v) {
        mergeAdd(IThemeApi.TOGGLE_BUTTON,
                new JsonBuilder().add("selectedHoverBackground", DrawableSerialization.serialize(v)));
        return getThis();
    }

    public B toggleButtonSelectedHoverBackground(String widgetTheme, String textureId) {
        mergeAdd(IThemeApi.TOGGLE_BUTTON, new JsonBuilder().add("selectedHoverBackground",
                new JsonBuilder().add("type", "texture").add("id", textureId)));
        return getThis();
    }

    public B toggleButtonSelectedColor(int v) {
        mergeAdd(IThemeApi.TOGGLE_BUTTON, new JsonBuilder().add("selectedColor", v));
        return getThis();
    }

    public B toggleButtonSelectedTextColor(int v) {
        mergeAdd(IThemeApi.TOGGLE_BUTTON, new JsonBuilder().add("selectedTextColor", v));
        return getThis();
    }

    public B toggleButtonSelectedTextShadow(boolean v) {
        mergeAdd(IThemeApi.TOGGLE_BUTTON, new JsonBuilder().add("selectedTextShadow", v));
        return getThis();
    }
}
