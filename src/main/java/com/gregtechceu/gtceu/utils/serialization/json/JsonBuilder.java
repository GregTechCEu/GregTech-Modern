package com.gregtechceu.gtceu.utils.serialization.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;

import java.util.Map;
import java.util.function.Consumer;

public class JsonBuilder {

    @Getter
    private final JsonObject json;

    public JsonBuilder(JsonObject json) {
        this.json = json;
    }

    public JsonBuilder() {
        this(new JsonObject());
    }

    public JsonBuilder add(String key, JsonElement element) {
        this.json.add(key, element);
        return this;
    }

    public JsonBuilder mergeAdd(String key, JsonObject element) {
        JsonElement merged = this.json.get(key);
        if (merged == null || !merged.isJsonObject()) return add(key, element);
        JsonObject mergedObj = merged.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : element.entrySet()) {
            mergedObj.add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public JsonBuilder add(String key, String element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder add(String key, Number element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder add(String key, boolean element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder add(String key, char element) {
        this.json.addProperty(key, element);
        return this;
    }

    public JsonBuilder add(String key, JsonBuilder element) {
        return add(key, element.getJson());
    }

    public JsonBuilder add(String key, JsonArrayBuilder element) {
        return add(key, element.getJson());
    }

    public JsonBuilder mergeAdd(String key, JsonBuilder element) {
        return mergeAdd(key, element.getJson());
    }

    public JsonBuilder addObject(String key, Consumer<JsonBuilder> builderConsumer) {
        JsonBuilder builder = new JsonBuilder();
        builderConsumer.accept(builder);
        return add(key, builder.getJson());
    }

    public JsonBuilder addArray(String key, Consumer<JsonArrayBuilder> builderConsumer) {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        builderConsumer.accept(builder);
        return add(key, builder.getJson());
    }

    public JsonBuilder addAllOf(JsonObject json) {
        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            this.json.add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    public JsonBuilder addAllOf(JsonBuilder json) {
        return addAllOf(json.getJson());
    }
}
