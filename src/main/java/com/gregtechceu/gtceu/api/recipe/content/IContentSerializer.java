package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public interface IContentSerializer<T> {

    default void toNetwork(FriendlyByteBuf buf, T content) {
        buf.writeJsonWithCodec(codec(), content);
    }

    default T fromNetwork(FriendlyByteBuf buf) {
        return buf.readJsonWithCodec(codec());
    }

    default T fromJson(JsonElement json) {
        return codec().parse(JsonOps.INSTANCE, json).getOrThrow(false, GTCEu.LOGGER::error);
    }

    default JsonElement toJson(T content) {
        return codec().encodeStart(JsonOps.INSTANCE, content).getOrThrow(false, GTCEu.LOGGER::error);
    }

    T of(Object o);

    T defaultValue();

    @SuppressWarnings("unchecked")
    default void toNetworkContent(FriendlyByteBuf buf, Content content) {
        T inner = (T) content.getContent();
        toNetwork(buf, inner);
        buf.writeVarInt(content.chance);
        buf.writeVarInt(content.maxChance);
        buf.writeVarInt(content.tierChanceBoost);
    }

    default Content fromNetworkContent(FriendlyByteBuf buf) {
        T inner = fromNetwork(buf);
        int chance = buf.readVarInt();
        int maxChance = buf.readVarInt();
        int tierChanceBoost = buf.readVarInt();
        return new Content(inner, chance, maxChance, tierChanceBoost);
    }

    Class<T> contentClass();

    Codec<T> codec();

    @SuppressWarnings("unchecked")
    default JsonElement toJsonContent(Content content) {
        JsonObject json = new JsonObject();
        json.add("content", toJson((T) content.getContent()));
        json.addProperty("chance", content.chance);
        json.addProperty("maxChance", content.maxChance);
        json.addProperty("tierChanceBoost", content.tierChanceBoost);
        return json;
    }

    default Content fromJsonContent(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        T inner = fromJson(jsonObject.get("content"));
        int chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsInt() : ChanceLogic.getMaxChancedValue();
        int maxChance = jsonObject.has("maxChance") ? jsonObject.get("maxChance").getAsInt() :
                ChanceLogic.getMaxChancedValue();
        int tierChanceBoost = jsonObject.has("tierChanceBoost") ? jsonObject.get("tierChanceBoost").getAsInt() : 0;
        return new Content(inner, chance, maxChance, tierChanceBoost);
    }

    default Tag toNbt(T content) {
        return codec().encodeStart(NbtOps.INSTANCE, content).getOrThrow(false, GTCEu.LOGGER::error);
    }

    default T fromNbt(Tag tag) {
        return codec().parse(NbtOps.INSTANCE, tag).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
