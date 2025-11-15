package com.gregtechceu.gtceu.api.mui.theme;

import com.gregtechceu.gtceu.api.mui.base.ITheme;
import com.gregtechceu.gtceu.api.mui.base.IThemeApi;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.*;

public class Theme extends AbstractTheme {

    private final WidgetThemeMap widgetThemes = new WidgetThemeMap();

    Theme(String id, ITheme parent, WidgetThemeMap widgetThemes) {
        super(id, parent);
        this.widgetThemes.putAll(widgetThemes);
        if (parent instanceof Theme theme) {
            for (WidgetThemeEntry<?> entry : theme.widgetThemes.values()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry);
                }
            }
        } else if (parent == DefaultTheme.INSTANCE) {
            if (!this.widgetThemes.containsKey(IThemeApi.FALLBACK)) {
                this.widgetThemes.putTheme(IThemeApi.FALLBACK, ThemeManager.defaultFallbackWidgetTheme);
            }
            for (WidgetThemeEntry<?> entry : DefaultTheme.INSTANCE.getWidgetThemes()) {
                if (!this.widgetThemes.containsKey(entry.getKey())) {
                    this.widgetThemes.put(entry.getKey(), entry);
                }
            }
        }
    }

    @Override
    public @UnmodifiableView Collection<WidgetThemeEntry<?>> getWidgetThemes() {
        return Collections.unmodifiableCollection(this.widgetThemes.values());
    }

    @Override
    public <T extends WidgetTheme> WidgetThemeEntry<T> getWidgetTheme(WidgetThemeKey<T> key) {
        WidgetThemeEntry<T> widgetTheme = this.widgetThemes.getTheme(key);
        while (widgetTheme == null && key.isSubWidgetTheme()) {
            widgetTheme = this.widgetThemes.getTheme(key.getParent());
        }
        return widgetTheme;
    }
}
