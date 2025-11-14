package com.gregtechceu.gtceu.api.mui.base;

import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.Scrollbar;
import com.gregtechceu.gtceu.api.mui.theme.*;
import com.gregtechceu.gtceu.client.mui.screen.ModularScreen;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;

import org.jetbrains.annotations.*;

import java.util.List;

/**
 * An API interface for Themes.
 */
@ApiStatus.NonExtendable
public interface IThemeApi {

    // widget themes
    WidgetThemeKey<WidgetTheme> FALLBACK = get().widgetThemeKeyBuilder("default", WidgetTheme.class)
            .defaultTheme(WidgetTheme.darkTextNoShadow(18, 18, null))
            .register();

    WidgetThemeKey<WidgetTheme> PANEL = get().widgetThemeKeyBuilder("panel", WidgetTheme.class)
            .defaultTheme(WidgetTheme.darkTextNoShadow(176, 166, GTGuiTextures.BACKGROUND))
            .register();

    WidgetThemeKey<WidgetTheme> BUTTON = get().widgetThemeKeyBuilder("button", WidgetTheme.class)
            .defaultTheme(WidgetTheme.whiteTextShadow(18, 18, GTGuiTextures.MC_BUTTON))
            .defaultHoverTheme(WidgetTheme.whiteTextShadow(18, 18, GTGuiTextures.MC_BUTTON_HOVERED))
            .register();

    WidgetThemeKey<WidgetTheme> CLOSE_BUTTON = get().widgetThemeKeyBuilder("closeButton", WidgetTheme.class)
            .defaultTheme(WidgetTheme.whiteTextShadow(10, 10, GTGuiTextures.MC_BUTTON))
            .defaultHoverTheme(WidgetTheme.whiteTextShadow(10, 10, GTGuiTextures.MC_BUTTON_HOVERED))
            .register();

    WidgetThemeKey<WidgetTheme> SCROLLBAR = get().widgetThemeKeyBuilder("scrollbar", WidgetTheme.class)
            .defaultTheme(WidgetTheme.darkTextNoShadow(4, 4, Scrollbar.VANILLA))
            .register();

    WidgetThemeKey<SlotTheme> ITEM_SLOT = get().widgetThemeKeyBuilder("itemSlot", SlotTheme.class)
            .defaultTheme(new SlotTheme(GTGuiTextures.SLOT))
            .register();

    WidgetThemeKey<SlotTheme> FLUID_SLOT = get().widgetThemeKeyBuilder("fluidSlot", SlotTheme.class)
            .defaultTheme(new SlotTheme(GTGuiTextures.FLUID_SLOT))
            .register();

    WidgetThemeKey<TextFieldTheme> TEXT_FIELD = get().widgetThemeKeyBuilder("textField", TextFieldTheme.class)
            .defaultTheme(new TextFieldTheme(0xFF2F72A8, 0xFF5F5F5F))
            .register();

    WidgetThemeKey<SelectableTheme> TOGGLE_BUTTON = get().widgetThemeKeyBuilder("toggleButton", SelectableTheme.class)
            .defaultTheme(
                    SelectableTheme.whiteTextShadow(18, 18, GTGuiTextures.MC_BUTTON, GTGuiTextures.MC_BUTTON_DISABLED))
            .defaultHoverTheme(SelectableTheme.whiteTextShadow(18, 18, GTGuiTextures.MC_BUTTON_HOVERED,
                    IDrawable.NONE))
            .register();

    // subwidget themes
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER = ITEM_SLOT.createSubKey("player");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_HOTBAR = ITEM_SLOT_PLAYER.createSubKey("playerHotbar");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_MAIN_INV = ITEM_SLOT_PLAYER.createSubKey("playerMainInventory");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_OFFHAND = ITEM_SLOT_PLAYER.createSubKey("playerOffhand");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_ARMOR = ITEM_SLOT_PLAYER.createSubKey("playerArmor");

    String HOVER_SUFFIX = ":hover";

    // properties
    String PARENT = "parent";
    String DEFAULT_WIDTH = "defaultWidth";
    String DEFAULT_HEIGHT = "defaultHeight";
    String BACKGROUND = "background";
    String HOVER_BACKGROUND = "hoverBackground";
    String COLOR = "color";
    String TEXT_COLOR = "textColor";
    String TEXT_SHADOW = "textShadow";
    String ICON_COLOR = "iconColor";
    String SLOT_HOVER_COLOR = "slotHoverColor";
    String MARKED_COLOR = "markedColor";
    String HINT_COLOR = "hintColor";
    String SELECTED_BACKGROUND = "selectedBackground";
    String SELECTED_COLOR = "selectedColor";
    String SELECTED_TEXT_COLOR = "selectedTextColor";
    String SELECTED_TEXT_SHADOW = "selectedTextShadow";
    String SELECTED_ICON_COLOR = "selectedIconColor";

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
     * Registers a theme json object. Themes from resource packs always have greater priority.
     * Json builders are used here as they are much easier to merge as opposed to normal java objects.
     *
     * @param id   id of the theme
     * @param json theme data
     */
    void registerTheme(String id, JsonBuilder json);

    /**
     * Registers a theme json object. Themes from resource packs always have greater priority.
     *
     * @param themeBuilder theme data
     */
    default void registerTheme(ThemeBuilder<?> themeBuilder) {
        registerTheme(themeBuilder.getId(), themeBuilder);
    }

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
     * Registers a widget theme. It is recommended to store the resulting key in a static variable and make it
     * accessible by public methods.
     *
     * @param id                id of the widget theme
     * @param defaultTheme      the fallback widget theme
     * @param defaultHoverTheme the fallback hover widget theme
     * @param parser            the widget theme json parser function. This is usually another constructor.
     * @return key to access the widget theme
     */
    <T extends WidgetTheme> WidgetThemeKey<T> registerWidgetTheme(String id, T defaultTheme, T defaultHoverTheme,
                                                                  WidgetThemeParser<T> parser);

    default <T extends WidgetTheme> WidgetThemeKeyBuilder<T> widgetThemeKeyBuilder(String id, Class<T> type) {
        return new WidgetThemeKeyBuilder<>(id);
    }

    @UnmodifiableView
    List<WidgetThemeKey<?>> getWidgetThemeKeys();
}
