package brachy.modularui.theme;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;

public class WidgetThemeMap extends Object2ObjectLinkedOpenHashMap<WidgetThemeKey<?>, WidgetThemeEntry<?>> {

    @Override
    public WidgetThemeEntry<?> put(WidgetThemeKey<?> widgetThemeKey, WidgetThemeEntry<?> widgetTheme) {
        if (widgetThemeKey != widgetTheme.key()) {
            throw new IllegalArgumentException(widgetThemeKey.getFullName() + " is not compatible with " +
                    widgetTheme.key().getFullName());
        }
        return super.put(widgetThemeKey, widgetTheme);
    }

    public <T extends WidgetTheme> void putTheme(WidgetThemeKey<T> key, WidgetThemeEntry<T> widgetTheme) {
        super.put(key, widgetTheme);
    }

    @SuppressWarnings("unchecked")
    public <T extends WidgetTheme> WidgetThemeEntry<T> getTheme(WidgetThemeKey<T> widgetThemeKey) {
        return (WidgetThemeEntry<T>) super.get(widgetThemeKey);
    }

    public <T extends WidgetTheme> void register(WidgetThemeKey<T> key, T widgetTheme, T hoverWidgetTheme) {
        putTheme(key, new WidgetThemeEntry<>(key, widgetTheme, hoverWidgetTheme));
    }
}
