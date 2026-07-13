package com.gregtechceu.gtceu.api.loot.serializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.Serializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record CodecBasedSerializer<T>(MapCodec<T> codec) implements Serializer<T> {

    private static final Logger LOGGER = LogManager.getLogger("GTCEu/CodecBasedLootSerializer");

    @Override
    public void serialize(JsonObject json, T value, JsonSerializationContext serializationContext) {
        JsonElement newJson = codec.codec().encodeStart(JsonOps.INSTANCE, value)
                .promotePartial(CodecBasedSerializer::logErrors)
                .getOrThrow(false, LOGGER::error);
        JsonObject asObject = GsonHelper.convertToJsonObject(newJson, "serialized value");
        json.asMap().putAll(asObject.asMap());
    }

    @Override
    public T deserialize(JsonObject json, JsonDeserializationContext serializationContext) {
        return codec.codec().parse(JsonOps.INSTANCE, json)
                .promotePartial(CodecBasedSerializer::logErrors)
                .getOrThrow(false, LOGGER::error);
    }

    private static void logErrors(String errorMessage) {
        LOGGER.error("Recoverable errors when parsing loot: {}", errorMessage);
    }
}
