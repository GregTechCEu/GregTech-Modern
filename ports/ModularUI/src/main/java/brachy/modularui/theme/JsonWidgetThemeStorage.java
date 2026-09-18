package brachy.modularui.theme;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;

import java.util.Map;

class JsonWidgetThemeStorage {

    private final Map<WidgetTheme, ImmutableJson> jsons = new Reference2ReferenceOpenHashMap<>();

    public <T extends WidgetTheme> ImmutableJson get(WidgetThemeKey<T> key, T theme) {
        ImmutableJson json = this.jsons.get(theme);
        if (json == null) {
            json = ImmutableJson.of(key.encodeJson(theme));
            this.jsons.put(theme, json);
        }
        return json;
    }
}
