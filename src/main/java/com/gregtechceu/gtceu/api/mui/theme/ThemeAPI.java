package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.api.mui.base.drawable.IDrawable;
import com.gregtechceu.gtceu.api.mui.drawable.GuiTextures;
import com.gregtechceu.gtceu.api.mui.utils.Color;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ThemeAPI implements IThemeApi {

    public static final ThemeAPI INSTANCE = new ThemeAPI();
    public static final String DEFAULT = "DEFAULT";
    public static final ITheme DEFAULT_DEFAULT = new DefaultTheme();

    private final Object2ObjectMap<String, ITheme> themes = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<String, List<JsonBuilder>> defaultThemes = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<String, WidgetTheme> defaultWidgetThemes = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<String, WidgetThemeParser> widgetThemeFunctions = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectOpenHashMap<String, String> jsonScreenThemes = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, String> screenThemes = new Object2ObjectOpenHashMap<>();

    private ThemeAPI() {
        registerWidgetTheme(Theme.PANEL,
                new WidgetTheme(GuiTextures.MC_BACKGROUND, null, Color.WHITE.main, 0xFF404040, false),
                WidgetTheme::new);
        registerWidgetTheme(Theme.BUTTON, new WidgetTheme(GuiTextures.MC_BUTTON, GuiTextures.MC_BUTTON_HOVERED,
                Color.WHITE.main, Color.WHITE.main, true), WidgetTheme::new);
        registerWidgetTheme(Theme.ITEM_SLOT,
                new WidgetSlotTheme(GuiTextures.SLOT_ITEM, Color.withAlpha(Color.WHITE.main, 0x60)),
                WidgetSlotTheme::new);
        registerWidgetTheme(Theme.FLUID_SLOT,
                new WidgetSlotTheme(GuiTextures.SLOT_FLUID, Color.withAlpha(Color.WHITE.main, 0x60)),
                WidgetSlotTheme::new);
        registerWidgetTheme(Theme.TEXT_FIELD, new WidgetTextFieldTheme(0xFF2F72A8, 0xFF5F5F5F),
                (parent, json, fallback) -> new WidgetTextFieldTheme(parent, fallback, json));
        registerWidgetTheme(Theme.TOGGLE_BUTTON,
                new WidgetThemeSelectable(GuiTextures.MC_BUTTON, GuiTextures.MC_BUTTON_HOVERED, Color.WHITE.main,
                        Color.WHITE.main, true,
                        GuiTextures.MC_BUTTON_DISABLED, IDrawable.NONE, Color.WHITE.main, Color.WHITE.main, true),
                WidgetThemeSelectable::new);
    }

    @Override
    public ITheme getDefaultTheme() {
        return DEFAULT_DEFAULT;
    }

    @Override
    public @NotNull ITheme getTheme(String id) {
        return this.themes.getOrDefault(id, getDefaultTheme());
    }

    @Override
    public boolean hasTheme(String id) {
        return this.themes.containsKey(id);
    }

    @Override
    public boolean hasWidgetTheme(String id) {
        return this.widgetThemeFunctions.containsKey(id);
    }

    @Override
    public void registerTheme(String id, JsonBuilder json) {
        List<JsonBuilder> themes = getJavaDefaultThemes(id);
        if (!themes.contains(json)) {
            themes.add(json);
        }
    }

    @Override
    public List<JsonBuilder> getJavaDefaultThemes(String id) {
        return this.defaultThemes.computeIfAbsent(id, key -> new ArrayList<>());
    }

    @Override
    public ITheme getThemeForScreen(String owner, String name, @Nullable String defaultTheme) {
        String theme = getThemeIdForScreen(owner, name);
        if (theme != null) return getTheme(theme);
        if (defaultTheme != null) return getTheme(defaultTheme);
        return getTheme(ConfigHolder.INSTANCE.client.ui.useDarkThemeByDefault ? "vanilla_dark" : "vanilla");
    }

    private String getThemeIdForScreen(String mod, String name) {
        String fullName = mod + ":" + name;
        String theme = this.jsonScreenThemes.get(fullName);
        if (theme != null) return theme;
        theme = this.jsonScreenThemes.get(mod);
        if (theme != null) return theme;
        theme = this.screenThemes.get(fullName);
        return theme != null ? theme : this.screenThemes.get(mod);
    }

    @Override
    public void registerThemeForScreen(String screen, String theme) {
        Objects.requireNonNull(screen);
        Objects.requireNonNull(theme);
        this.screenThemes.put(screen, theme);
    }

    @Override
    public void registerWidgetTheme(String id, WidgetTheme defaultTheme, WidgetThemeParser parser) {
        if (this.widgetThemeFunctions.containsKey(id)) {
            throw new IllegalStateException();
        }
        this.widgetThemeFunctions.put(id, parser);
        this.defaultWidgetThemes.put(id, defaultTheme);
    }

    // Internals

    void registerTheme(ITheme theme) {
        if (this.themes.containsKey(theme.getId())) {
            throw new IllegalArgumentException("Theme with id " + theme.getId() + " already exists!");
        }
        this.themes.put(theme.getId(), theme);
    }

    void onReload() {
        this.themes.clear();
        this.jsonScreenThemes.clear();
        registerTheme(DEFAULT_DEFAULT);
    }

    public static class DefaultTheme extends AbstractDefaultTheme {

        private DefaultTheme() {}

        @Override
        public String getId() {
            return DEFAULT;
        }

        @Override
        public WidgetTheme getFallback() {
            return ThemeManager.defaultdefaultWidgetTheme;
        }

        @Override
        public WidgetTheme getWidgetTheme(String id) {
            return INSTANCE.defaultWidgetThemes.get(id);
        }
    }
}
