package com.gregtechceu.gtceu.api.mui.base;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.ApiStatus;

public interface IJsonSerializable<T extends IJsonSerializable<T>> {

    /**
     * Override this
     * 
     * @return the codec to serialize this object with
     */
    // TODO actually implement on subclasses
    @ApiStatus.OverrideOnly
    default Codec<T> getCodec() {
        return Codec.PASSTHROUGH.flatComapMap(dynamic -> {
            loadFromJson(dynamic.cast(JsonOps.INSTANCE).getAsJsonObject());
            return (T) this;
        }, object -> {
            JsonObject jsonObject = new JsonObject();
            if (saveToJson(jsonObject)) {
                return DataResult.success(new Dynamic<>(JsonOps.INSTANCE, jsonObject));
            }
            return DataResult.error(() -> "Failed to serialize drawable %s".formatted(object));
        });
    }

    /**
     * Reads extra json data after this drawable is created.
     *
     * @param json json to read from
     */
    default void loadFromJson(JsonObject json) {}

    /**
     * Writes all json data necessary so that deserializing it results in the same drawable.
     *
     * @param json json to write to
     * @return if the drawable was serialized
     */
    default boolean saveToJson(JsonObject json) {
        return false;
    }
}
