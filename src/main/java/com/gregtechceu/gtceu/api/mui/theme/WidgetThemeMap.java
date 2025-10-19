package com.gregtechceu.gtceu.api.mui.theme;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class WidgetThemeMap extends Object2ObjectOpenHashMap<WidgetThemeKey<?>, WidgetThemeEntry<?>> {

    @Override
    public WidgetThemeEntry<?> put(WidgetThemeKey<?> widgetThemeKey, WidgetThemeEntry<?> widgetTheme) {
        if (widgetThemeKey != widgetTheme.getKey()) {
            throw new IllegalArgumentException(widgetThemeKey.getFullName() + " is not compatible with " +
                    widgetTheme.getKey().getFullName());
        }
        return super.put(widgetThemeKey, widgetTheme);
    }

    @SuppressWarnings("unchecked")
    public <T extends WidgetTheme> WidgetThemeEntry<T> putTheme(WidgetThemeKey<T> widgetThemeKey,
                                                                WidgetThemeEntry<T> widgetTheme) {
        return (WidgetThemeEntry<T>) super.put(widgetThemeKey, widgetTheme);
    }

    @SuppressWarnings("unchecked")
    public <T extends WidgetTheme> WidgetThemeEntry<T> getTheme(WidgetThemeKey<T> widgetThemeKey) {
        return (WidgetThemeEntry<T>) super.get(widgetThemeKey);
    }
}
