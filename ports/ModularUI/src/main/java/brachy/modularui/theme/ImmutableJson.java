package brachy.modularui.theme;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;

import java.util.Collections;
import java.util.Map;

public class ImmutableJson {

    public static final ImmutableJson EMPTY = new ImmutableJson(Collections.emptyMap());

    private final Map<String, JsonElement> json;

    public static ImmutableJson of(JsonObject json) {
        if (json == null || json.asMap().isEmpty()) {
            return EMPTY;
        }
        return new ImmutableJson(json);
    }

    private ImmutableJson(Map<String, JsonElement> json) {
        this.json = json;
    }

    private ImmutableJson(JsonObject json) {
        this(new Object2ReferenceOpenHashMap<>(json.asMap()));
    }

    public boolean has(String key) {
        return this.json.containsKey(key);
    }

    public JsonElement get(String key) {
        return this.json.get(key);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.asMap().putAll(this.json);
        return json;
    }
}
