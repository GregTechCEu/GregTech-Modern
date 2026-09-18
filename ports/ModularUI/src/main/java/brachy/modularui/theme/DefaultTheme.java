package brachy.modularui.theme;

import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;

public class DefaultTheme extends AbstractTheme {

    public static final DefaultTheme INSTANCE = new DefaultTheme();

    private final WidgetThemeMap widgetThemes = new WidgetThemeMap();
    private boolean initialized = false;

    private DefaultTheme() {
        super(ThemeAPI.DEFAULT_ID, null);
    }

    private void initialize() {
        if (!initialized) {
            initialized = true;
            for (var key : ThemeAPI.INSTANCE.getWidgetThemeKeys()) {
                this.widgetThemes.put(key, entryOfKey(key));
            }
        }
    }

    @Override
    public @UnmodifiableView Collection<WidgetThemeEntry<?>> getWidgetThemes() {
        initialize();
        return Collections.unmodifiableCollection(this.widgetThemes.values());
    }

    @Override
    public <T extends WidgetTheme> WidgetThemeEntry<T> getWidgetTheme(WidgetThemeKey<T> key) {
        initialize();
        WidgetThemeEntry<T> widgetTheme = this.widgetThemes.getTheme(key);
        while (widgetTheme == null && key.isSubWidgetTheme()) {
            widgetTheme = this.widgetThemes.getTheme(key.getParent());
        }
        return widgetTheme;
    }

    private static <T extends WidgetTheme> WidgetThemeEntry<T> entryOfKey(WidgetThemeKey<T> key) {
        return new WidgetThemeEntry<>(key, key.getDefaultValue(), key.getDefaultHoverValue());
    }
}
