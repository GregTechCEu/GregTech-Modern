package com.gregtechceu.gtceu.api.mui.theme;

import com.google.gson.JsonObject;

/**
 * An interface used to parse json objects to widget themes.
 */
@FunctionalInterface
public interface WidgetThemeParser {

    /**
     * Parses a json object to a widget theme,
     *
     * @param parent   the widget theme from the parent of the currently parsed theme
     * @param json     the widget theme json data object
     * @param fallback a fallback widget theme json data object
     * @return the parsed widget theme
     */
    WidgetTheme parse(WidgetTheme parent, JsonObject json, JsonObject fallback);
}
