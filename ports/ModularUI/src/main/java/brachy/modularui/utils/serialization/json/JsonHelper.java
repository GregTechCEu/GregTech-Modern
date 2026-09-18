package brachy.modularui.utils.serialization.json;

import brachy.modularui.ModularUI;
import brachy.modularui.utils.serialization.codec.MutableCodec;

import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.JsonOps;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("JavaExistingMethodCanBeUsed")
public class JsonHelper {

    public static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public static JsonElement serialize(Object object) {
        return GSON.toJsonTree(object);
    }

    public static <T> T deserialize(JsonElement json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }

    public static <T> T deserialize(JsonObject json, Class<T> clazz, T defaultValue, String... keys) {
        JsonElement element = getJsonElement(json, keys);
        if (element != null) {
            T t = deserialize(element, clazz);
            return t == null ? defaultValue : t;
        }
        return defaultValue;
    }

    public static <T> T deserializeWithFallback(JsonObject json, JsonObject fallback, Class<T> clazz, T defaultValue,
                                                String... keys) {
        T t = deserialize(json, clazz, null, keys);
        return t != null ? t : deserialize(fallback, clazz, defaultValue, keys);
    }

    public static float getFloat(JsonObject json, float defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsFloat();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static int getInt(JsonObject json, int defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsInt();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static int getIntWithFallback(JsonObject json, JsonObject fallback, int defaultValue,
                                         String @NotNull ... keys) {
        Integer i = getBoxedInt(json, null, keys);
        return i != null ? i : getInt(fallback, defaultValue, keys);
    }

    public static boolean getBoolean(JsonObject json, boolean defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsBoolean();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static boolean getBoolWithFallback(JsonObject json, JsonObject fallback, boolean defaultValue,
                                              String @NotNull ... keys) {
        Boolean i = getBoxedBool(json, null, keys);
        return i != null ? i : getBoolean(fallback, defaultValue, keys);
    }

    public static String getString(JsonObject json, String defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return jsonElement.getAsString();
            }
        }
        return defaultValue;
    }

    public static <T> T getObject(JsonObject json, T defaultValue, Function<JsonObject, T> factory,
                                  String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonObject()) {
                    return factory.apply(jsonElement.getAsJsonObject());
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static <T> T getElement(JsonObject json, T defaultValue, Function<JsonElement, T> factory,
                                   String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                return factory.apply(jsonElement);
            }
        }
        return defaultValue;
    }

    public static @Nullable Integer getBoxedInt(JsonObject json, Integer defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsInt();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static @Nullable Boolean getBoxedBool(JsonObject json, Boolean defaultValue, String @NotNull ... keys) {
        if (json == null) return defaultValue;
        for (String key : keys) {
            if (json.has(key)) {
                JsonElement jsonElement = json.get(key);
                if (jsonElement.isJsonPrimitive()) {
                    return jsonElement.getAsBoolean();
                }
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public static @Nullable JsonElement getJsonElement(JsonObject json, String @NotNull ... keys) {
        if (json == null) return null;
        for (String key : keys) {
            if (json.has(key)) {
                return json.get(key);
            }
        }
        return null;
    }

    public static JsonElement parse(InputStream inputStream) {
        return JsonParser.parseReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
    }

    public static JsonObject merge(JsonObject base, JsonObject other) {
        for (Map.Entry<String, JsonElement> entry : other.entrySet()) {
            base.add(entry.getKey(), entry.getValue());
        }
        return base;
    }

    public static JsonObject makeJson(Consumer<JsonObject> writer) {
        JsonObject json = new JsonObject();
        writer.accept(json);
        return json;
    }

    public static <T> JsonElement toJson(Encoder<T> codec, T input) {
        var d = codec.encodeStart(JsonOps.INSTANCE, input);
        if (d.error().isPresent()) ModularUI.LOGGER.error("Error encoding '{}' to json: {}", input, d.error().get());
        return d.result().orElse(JsonNull.INSTANCE);
    }

    public static <T> String toJsonString(Encoder<T> codec, T input) {
        return GSON.toJson(toJson(codec, input));
    }

    public static <T> T fromJson(MutableCodec<T> codec, JsonElement json, T instance) {
        var d = codec.parse(JsonOps.INSTANCE, json, instance);
        if (d.error().isPresent()) ModularUI.LOGGER.error("Error decoding from json: {}", d.error().get());
        return instance;
    }

    public static <T> T fromJson(Decoder<T> codec, JsonElement json) {
        var d = codec.parse(JsonOps.INSTANCE, json);
        if (d.error().isPresent()) ModularUI.LOGGER.error("Error decoding from json: {}", d.error().get());
        return d.result().orElseThrow();
    }

    public static <T> T fromJsonString(MutableCodec<T> codec, String json, T instance) {
        return fromJson(codec, JsonParser.parseString(json), instance);
    }

    public static <T> T fromJsonString(Decoder<T> codec, String json) {
        return fromJson(codec, JsonParser.parseString(json));
    }
}
