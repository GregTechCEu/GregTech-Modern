package brachy.modularui.api;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.Scrollbar;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.ModularScreen;
import brachy.modularui.theme.SelectableTheme;
import brachy.modularui.theme.SlotTheme;
import brachy.modularui.theme.TextFieldTheme;
import brachy.modularui.theme.ThemeAPI;
import brachy.modularui.theme.ThemeBuilder;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeCodec;
import brachy.modularui.theme.WidgetThemeKey;
import brachy.modularui.theme.WidgetThemeKeyBuilder;
import brachy.modularui.theme.WidgetThemeMerger;
import brachy.modularui.utils.Color;
import brachy.modularui.utils.serialization.json.JsonBuilder;

import com.mojang.serialization.Codec;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;

/**
 * An API interface for Themes.
 */
@ApiStatus.NonExtendable
public interface IThemeApi {
    // @formatter:off

    // properties
    String PARENT = "parent";
    String DEFAULT_WIDTH = "defaultWidth";
    String DEFAULT_HEIGHT = "defaultHeight";
    String BACKGROUND = "background";
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

    // widget themes
    WidgetThemeKey<WidgetTheme> FALLBACK = get().widgetThemeKeyBuilder("default", WidgetTheme.class)
            .defaultTheme(WidgetTheme.darkTextNoShadow(18, 18, null))
            .field(DEFAULT_WIDTH, int.class, Codec.INT, WidgetTheme::getDefaultWidth)
            .field(DEFAULT_HEIGHT, int.class, Codec.INT, WidgetTheme::getDefaultHeight)
            .field(BACKGROUND, IDrawable.class, IDrawable.CODEC, WidgetTheme::getBackground)
            .fallbackField(COLOR, int.class, Color.CODEC, WidgetTheme::getColor)
            .fallbackField(TEXT_COLOR, int.class, Color.CODEC, WidgetTheme::getTextColor)
            .fallbackField(TEXT_SHADOW, boolean.class, Codec.BOOL, WidgetTheme::isTextShadow)
            .fallbackField(ICON_COLOR, int.class, Color.CODEC, WidgetTheme::getIconColor)
            .register();

    WidgetThemeKey<WidgetTheme> PANEL = get().widgetThemeKeyBuilder("panel", WidgetTheme.class)
            .defaultTheme(WidgetTheme.darkTextNoShadow(176, 166, GuiTextures.MC_BACKGROUND))
            .fieldsOf(FALLBACK)
            .register();

    WidgetThemeKey<WidgetTheme> BUTTON = get().widgetThemeKeyBuilder("button", WidgetTheme.class)
            .defaultTheme(WidgetTheme.whiteTextShadow(18, 18, GuiTextures.MC_BUTTON))
            .defaultHoverTheme(WidgetTheme.whiteTextShadow(18, 18, GuiTextures.MC_BUTTON_HOVERED))
            .fieldsOf(FALLBACK)
            .register();

    WidgetThemeKey<WidgetTheme> CLOSE_BUTTON = get().widgetThemeKeyBuilder("closeButton", WidgetTheme.class)
            .defaultTheme(WidgetTheme.whiteTextShadow(10, 10, GuiTextures.MC_BUTTON))
            .defaultHoverTheme(WidgetTheme.whiteTextShadow(10, 10, GuiTextures.MC_BUTTON_HOVERED))
            .fieldsOf(FALLBACK)
            .register();

    WidgetThemeKey<WidgetTheme> SCROLLBAR = get().widgetThemeKeyBuilder("scrollbar", WidgetTheme.class)
            .defaultTheme(WidgetTheme.darkTextNoShadow(4, 4, Scrollbar.VANILLA))
            .fieldsOf(FALLBACK)
            .register();

    WidgetThemeKey<SlotTheme> ITEM_SLOT = get().widgetThemeKeyBuilder("itemSlot", SlotTheme.class)
            .defaultTheme(new SlotTheme(GuiTextures.SLOT_ITEM))
            .fieldsOf(FALLBACK)
            .field(SLOT_HOVER_COLOR, int.class, Color.CODEC, SlotTheme::getSlotHoverColor)
            .register();

    WidgetThemeKey<SlotTheme> FLUID_SLOT = get().widgetThemeKeyBuilder("fluidSlot", SlotTheme.class)
            .defaultTheme(new SlotTheme(GuiTextures.SLOT_FLUID))
            .fieldsOf(ITEM_SLOT)
            .register();

    WidgetThemeKey<TextFieldTheme> TEXT_FIELD = get().widgetThemeKeyBuilder("textField", TextFieldTheme.class)
            .defaultTheme(new TextFieldTheme(0xFF2F72A8, 0xFF5F5F5F))
            .fieldsOf(FALLBACK)
            .field(MARKED_COLOR, int.class, Color.CODEC, TextFieldTheme::getMarkedColor)
            .field(HINT_COLOR, int.class, Color.CODEC, TextFieldTheme::getHintColor)
            .register();

    WidgetThemeKey<SelectableTheme> TOGGLE_BUTTON = get().widgetThemeKeyBuilder("toggleButton", SelectableTheme.class)
            .defaultTheme(SelectableTheme.whiteTextShadow(18, 18, GuiTextures.MC_BUTTON, GuiTextures.MC_BUTTON_DISABLED))
            .defaultHoverTheme(SelectableTheme.whiteTextShadow(18, 18, GuiTextures.MC_BUTTON_HOVERED, IDrawable.NONE))
            .fieldsOf(FALLBACK)
            .field(SELECTED_BACKGROUND, IDrawable.class, IDrawable.CODEC, SelectableTheme::getSelectedBackground)
            .field(SELECTED_COLOR, int.class, Color.CODEC, SelectableTheme::getSelectedColor)
            .field(SELECTED_TEXT_COLOR, int.class, Color.CODEC, SelectableTheme::getSelectedTextColor)
            .field(SELECTED_TEXT_SHADOW, boolean.class, Codec.BOOL, SelectableTheme::isSelectedTextShadow)
            .field(SELECTED_ICON_COLOR, int.class, Color.CODEC, SelectableTheme::getSelectedIconColor)
            .register();

    // subwidget themes
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER = ITEM_SLOT.createSubKey("player");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_HOTBAR = ITEM_SLOT_PLAYER.createSubKey("playerHotbar");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_MAIN_INV = ITEM_SLOT_PLAYER.createSubKey("playerMainInventory");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_OFFHAND = ITEM_SLOT_PLAYER.createSubKey("playerOffhand");
    WidgetThemeKey<SlotTheme> ITEM_SLOT_PLAYER_ARMOR = ITEM_SLOT_PLAYER.createSubKey("playerArmor");

    String HOVER_SUFFIX = ":hover";


    // @formatter:on

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
     * Call this during {@link net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent FMLConstructModEvent}.
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
    default ITheme getThemeForScreen(String owner, String name, @Nullable String defaultTheme, @Nullable String fallbackTheme) {
        return getThemeForScreen(owner, name, null, defaultTheme, fallbackTheme);
    }

    /**
     * Gets the appropriate theme for a screen.
     *
     * @param owner        owner of the screen
     * @param name         name of the screen
     * @param panel        the name
     * @param defaultTheme default theme if no theme was found
     * @return the registered theme for the given screen or the given default theme or {@link #getDefaultTheme()}
     */
    ITheme getThemeForScreen(String owner, String name, @Nullable String panel, @Nullable String defaultTheme, @Nullable String fallbackTheme);

    /**
     * Gets the appropriate theme for a specific panel.
     *
     * @param panel        the panel to find a theme for
     * @param defaultTheme default theme if no theme was found
     * @return the registered theme for the given screen or the given default theme or {@link #getDefaultTheme()}
     */
    default ITheme getThemeForScreen(ModularPanel<?> panel, @Nullable String defaultTheme) {
        ModularScreen screen = panel.getScreen();
        return getThemeForScreen(screen.getOwner(), screen.getName(), panel.getName(), defaultTheme, screen.getThemeOverride());
    }

    /**
     * Gets the appropriate theme for a screen.
     *
     * @param screen       screen
     * @param defaultTheme default theme if no theme was found
     * @return the registered theme for the given screen or the given default theme or {@link #getDefaultTheme()}
     */
    default ITheme getThemeForScreen(ModularScreen screen, @Nullable String defaultTheme) {
        return getThemeForScreen(screen.getOwner(), screen.getName(), defaultTheme, null);
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

    @Deprecated
    <T extends WidgetTheme> WidgetThemeKey<T> registerWidgetTheme(String id, T defaultTheme, T defaultHoverTheme,
                                                                  WidgetThemeMerger<T> merger, WidgetThemeCodec<T> codec);

    default <T extends WidgetTheme> WidgetThemeKeyBuilder<T> widgetThemeKeyBuilder(String id, Class<T> type) {
        return new WidgetThemeKeyBuilder<>(id, type);
    }

    @UnmodifiableView
    List<WidgetThemeKey<?>> getWidgetThemeKeys();
}
