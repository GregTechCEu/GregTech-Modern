package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.IThemeApi;

import com.google.gson.JsonObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

public class WidgetThemeKeyBuilder<T extends WidgetTheme> {

    private final String id;
    private T defaultTheme;
    private T defaultHoverTheme;
    private WidgetThemeParser<T> parser;

    public WidgetThemeKeyBuilder(String id) {
        this.id = id;
    }

    public WidgetThemeKeyBuilder<T> defaultTheme(T defaultTheme) {
        this.defaultTheme = defaultTheme;
        return this;
    }

    public WidgetThemeKeyBuilder<T> defaultHoverTheme(T defaultHoverTheme) {
        this.defaultHoverTheme = defaultHoverTheme;
        return this;
    }

    public WidgetThemeKeyBuilder<T> parser(WidgetThemeParser<T> parser) {
        this.parser = parser;
        return this;
    }

    @SuppressWarnings("unchecked")
    public WidgetThemeKey<T> register() {
        Objects.requireNonNull(this.id, "Id for widget theme must not be null");
        Objects.requireNonNull(this.defaultTheme,
                "Default theme for widget theme must not be null, but is null for id '" + this.id + "'.");
        if (parser == null) parser = createParserWithReflection();
        if (defaultHoverTheme == null) {
            WidgetTheme hover = this.defaultTheme.withNoHoverBackground();
            if (hover.getClass() != this.defaultTheme.getClass()) {
                throw new IllegalArgumentException(
                        "Tried to create a default hover theme, but method withNoHoverBackground()" +
                                "is not overridden to create its type");
            }
            defaultHoverTheme = (T) hover;
        }
        return IThemeApi.get().registerWidgetTheme(this.id, this.defaultTheme, this.defaultHoverTheme, this.parser);
    }

    @SuppressWarnings("unchecked")
    private WidgetThemeParser<T> createParserWithReflection() {
        Class<T> type = (Class<T>) this.defaultTheme.getClass();
        try {
            Constructor<T> ctor = type.getConstructor(type, JsonObject.class, JsonObject.class);
            return (parent, json, fallback) -> {
                try {
                    return ctor.newInstance(parent, json, fallback);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException("Failed to instantiate widget theme of type " + type.getSimpleName(), e);
                }
            };
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format(
                    "No constructor with signature '%s(%s parent, JsonObject json, JsonObject fallback)' found",
                    type.getSimpleName(), type.getSimpleName()));
        }
    }
}
