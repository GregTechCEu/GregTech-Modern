package brachy.modularui.utils.serialization.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.function.Consumer;

public record JsonArrayBuilder(JsonArray json) {

    public JsonArrayBuilder() {
        this(new JsonArray());
    }

    public JsonArrayBuilder add(boolean element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(char element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(Number element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(String element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(JsonElement element) {
        this.json.add(element);
        return this;
    }

    public JsonArrayBuilder add(JsonBuilder element) {
        return add(element.getJson());
    }

    public JsonArrayBuilder add(JsonArrayBuilder element) {
        return add(element.json());
    }

    public JsonArrayBuilder addObject(Consumer<JsonBuilder> builderConsumer) {
        JsonBuilder builder = new JsonBuilder();
        builderConsumer.accept(builder);
        return add(builder.getJson());
    }

    public JsonArrayBuilder addArray(Consumer<JsonArrayBuilder> builderConsumer) {
        JsonArrayBuilder builder = new JsonArrayBuilder();
        builderConsumer.accept(builder);
        return add(builder.json());
    }

    public JsonArrayBuilder addAllOf(JsonArray json) {
        for (JsonElement element : json) {
            this.json.add(element);
        }
        return this;
    }

    public JsonArrayBuilder addAllOf(JsonArrayBuilder json) {
        return addAllOf(json.json());
    }
}
