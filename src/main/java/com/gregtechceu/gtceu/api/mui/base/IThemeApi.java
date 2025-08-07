package com.gregtechceu.gtceu.api.mui.base;

import com.gregtechceu.gtceu.api.mui.theme.ThemeAPI;
import com.gregtechceu.gtceu.api.mui.theme.WidgetTheme;
import com.gregtechceu.gtceu.api.mui.theme.WidgetThemeParser;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * An API interface for Themes.
 */
public interface IThemeApi {

    // widget themes
    String FALLBACK = "default";
    String PANEL = "panel";
    String BUTTON = "button";
    String ITEM_SLOT = "itemSlot";
    String FLUID_SLOT = "fluidSlot";
    String TEXT_FIELD = "textField";
    String TOGGLE_BUTTON = "toggleButton";

    // properties
    String PARENT = "parent";
    String BACKGROUND = "background";
    String HOVER_BACKGROUND = "hoverBackground";
    String COLOR = "color";
    String TEXT_COLOR = "textColor";
    String TEXT_SHADOW = "textShadow";
    String SLOT_HOVER_COLOR = "slotHoverColor";
    String MARKED_COLOR = "markedColor";
    String HINT_COLOR = "hintColor";
    String SELECTED_BACKGROUND = "selectedBackground";
    String SELECTED_HOVER_BACKGROUND = "selectedHoverBackground";
    String SELECTED_COLOR = "selectedColor";
    String SELECTED_TEXT_COLOR = "selectedTextColor";
    String SELECTED_TEXT_SHADOW = "selectedTextShadow";

    /**
     * @return the default api implementation
     */
    @Contract(pure = true)
    static IThemeApi get() {
        return ThemeAPI.INSTANCE;
    }

    /**
     * @return the absolute fallback theme
     */
    ITheme getDefaultTheme();

    /**
     * Finds a theme for an id
     *
     * @param id id of the theme
     * @return the found theme or {@link #getDefaultTheme()} if no theme was found
     */
    @NotNull
    ITheme getTheme(String id);

    /**
     * @param id id of the theme
     * @return if a theme with the id is registered
     */
    boolean hasTheme(String id);

    /**
     * @param id id of the widget theme
     * @return if a widget theme with the id is registered
     */
    boolean hasWidgetTheme(String id);

    /**
     * Registers a theme json object. Themes from resource packs always have greater priority.
     *
     * @param id   id of the theme
     * @param json theme data
     */
    void registerTheme(String id, JsonBuilder json);

    /**
     * Gets all currently from java side registered theme json's for a theme.
     *
     * @param id id of the theme
     * @return all theme json's for a theme.
     */
    List<JsonBuilder> getJavaDefaultThemes(String id);

    /**
     * Gets the appropriate theme for a screen.
     *
     * @param owner        owner of the screen
     * @param name         name of the screen
     * @param defaultTheme default theme if no theme was found
     * @return the registered theme for the given screen or the given default theme or {@link #getDefaultTheme()}
     */
    ITheme getThemeForScreen(String owner, String name, @Nullable String defaultTheme);

    /**
     * Gets the appropriate theme for a screen.
     *
     * @param screen       screen
     * @param defaultTheme default theme if no theme was found
     * @return the registered theme for the given screen or the given default theme or {@link #getDefaultTheme()}
     */
    default ITheme getThemeForScreen(ModularScreen screen, @Nullable String defaultTheme) {
        return getThemeForScreen(screen.getOwner(), screen.getName(), defaultTheme);
    }

    /**
     * Registers a theme for a screen. Themes from resource packs always have greater priority.
     *
     * @param owner owner of the screen
     * @param name  name of the screen
     * @param theme theme to register
     */
    default void registerThemeForScreen(String owner, String name, String theme) {
        registerThemeForScreen(owner + ":" + name, theme);
    }

    /**
     * Registers a theme for a screen. Themes from resource packs always have greater priority.
     *
     * @param screen full screen id
     * @param theme  theme to register
     */
    void registerThemeForScreen(String screen, String theme);

    /**
     * Register a widget theme.
     *
     * @param id           id of the widget theme
     * @param defaultTheme the fallback widget theme
     * @param parser       the widget theme json parser function
     */
    void registerWidgetTheme(String id, WidgetTheme defaultTheme, WidgetThemeParser parser);
}
