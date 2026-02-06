package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.utils.serialization.json.JsonBuilder;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ThemeAPI implements IThemeApi {

    public static final ThemeAPI INSTANCE = new ThemeAPI();
    public static final String DEFAULT_ID = "DEFAULT";
    public static final ITheme DEFAULT_THEME = DefaultTheme.INSTANCE;

    public static final Pattern widgetThemeNamePattern = Pattern.compile("[a-zA-z0-9$_-]+");

    private final Object2ObjectMap<String, ITheme> themes = new Object2ObjectOpenHashMap<>();
    protected final Object2ObjectMap<String, List<JsonBuilder>> defaultThemes = new Object2ObjectOpenHashMap<>();
    private final List<WidgetThemeKey<?>> keys = new ArrayList<>();
    protected final Object2ObjectOpenHashMap<String, String> jsonScreenThemes = new Object2ObjectOpenHashMap<>();
    private final Object2ObjectMap<String, String> screenThemes = new Object2ObjectOpenHashMap<>();

    private ThemeAPI() {}

    @Override
    public ITheme getDefaultTheme() {
        return DEFAULT_THEME;
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
    public ITheme getThemeForScreen(String owner, String name, @Nullable String panel, @Nullable String defaultTheme,
                                    @Nullable String fallbackTheme) {
        String theme = getThemeIdForScreen(owner, name, panel);
        if (theme != null) return getTheme(theme);
        if (defaultTheme != null) return getTheme(defaultTheme);
        if (fallbackTheme != null) return getTheme(fallbackTheme);
        return getTheme(ConfigHolder.INSTANCE.client.ui.useDarkThemeByDefault ? "vanilla_dark" : "vanilla");
    }

    private String getThemeIdForScreen(String mod, String name, String panelName) {
        String fullName = mod + ":" + name;
        String fullPanelName = null;
        if (panelName != null) fullPanelName = fullName + ":" + panelName;
        String theme = null;
        if (fullPanelName != null) {
            theme = this.jsonScreenThemes.get(fullPanelName);
            if (theme != null) return theme;
        }
        theme = this.jsonScreenThemes.get(fullName);
        if (theme != null) return theme;
        theme = this.jsonScreenThemes.get(mod);
        if (theme != null) return theme;
        if (fullPanelName != null) {
            theme = this.screenThemes.get(fullPanelName);
            if (theme != null) return theme;
        }
        theme = this.screenThemes.get(fullName);
        return theme != null ? theme : this.screenThemes.get(mod);
    }

    @Override
    public void registerThemeForScreen(String screen, String theme) {
        Objects.requireNonNull(screen);
        Objects.requireNonNull(theme);
        this.screenThemes.put(screen, theme);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends WidgetTheme> WidgetThemeKey<T> registerWidgetTheme(String id, T defaultTheme, T defaultHoverTheme,
                                                                         WidgetThemeParser<T> parser) {
        Objects.requireNonNull(id, "Id for widget theme must not be null");
        Objects.requireNonNull(defaultTheme, "Default widget theme must not be null, but is null for id '" + id + "'.");
        Objects.requireNonNull(parser, "Parser for widget theme must not be null, but is null for id '" + id + "'.");
        if (WidgetThemeKey.getFromFullName(id) != null) {
            throw new IllegalStateException("there already is a widget theme for id '" + id + "' registered.");
        }
        if (!widgetThemeNamePattern.matcher(id).matches()) {
            throw new IllegalArgumentException("Widget theme id '" + id +
                    "' is invalid. Id must only contain letters, numbers, underscores and minus.");
        }
        Class<T> type = (Class<T>) defaultTheme.getClass();
        return new WidgetThemeKey<>(type, id, defaultTheme, defaultHoverTheme, parser);
    }

    @Override
    public List<WidgetThemeKey<?>> getWidgetThemeKeys() {
        return Collections.unmodifiableList(this.keys);
    }

    // Internals

    void registerTheme(ITheme theme) {
        if (this.themes.containsKey(theme.getId())) {
            throw new IllegalArgumentException("Theme with id " + theme.getId() + " already exists!");
        }
        this.themes.put(theme.getId(), theme);
    }

    void registerWidgetThemeKey(WidgetThemeKey<?> key) {
        this.keys.add(key);
    }

    void onReload() {
        this.themes.clear();
        this.jsonScreenThemes.clear();
        registerTheme(DEFAULT_THEME);
    }
}
