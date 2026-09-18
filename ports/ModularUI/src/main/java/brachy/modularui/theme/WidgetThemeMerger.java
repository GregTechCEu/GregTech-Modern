package brachy.modularui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Handles merging widget theme JSONs into a single one and inherits values properly
 *
 * @param <T>
 */
public class WidgetThemeMerger<T extends WidgetTheme> {

    private final List<WidgetThemeField<T, ?>> fields;

    public WidgetThemeMerger(List<WidgetThemeField<T, ?>> fields) {
        this.fields = fields;
    }

    public JsonObject merge(JsonObject input, ImmutableJson parent, ImmutableJson fallback) {
        var inheritsList = getInheritsList(input);
        for (WidgetThemeField<T, ?> field : this.fields) {
            mergeField(field, input, parent, fallback, inheritsList);
        }
        return input;
    }

    private <V> void mergeField(WidgetThemeField<T, V> field, JsonObject input, ImmutableJson parent, ImmutableJson fallback, JsonElement inheritsList) {
        var key = field.name();
        if (input.has(key)) return;
        if (field.canFallback() && !inherits(inheritsList, key) && fallback.has(key)) {
            input.add(key, fallback.get(key));
            return;
        }
        if (parent.has(key)) {
            input.add(key, parent.get(key));
        }
    }

    private static JsonElement getInheritsList(JsonObject json) {
        if (!json.has("inherit")) return JsonNull.INSTANCE;
        return json.get("inherit");
    }

    protected static boolean inherits(JsonElement element, String property) {
        if (element == JsonNull.INSTANCE) return false;
        if (element.isJsonPrimitive()) return element.getAsString().equals(property);
        if (element.isJsonArray()) {
            for (JsonElement e : element.getAsJsonArray()) {
                if (e.isJsonPrimitive() && e.getAsString().equals(property)) return true;
            }
        }
        return false;
    }
}
