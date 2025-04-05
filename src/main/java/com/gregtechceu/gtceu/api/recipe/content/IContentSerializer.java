package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.api.registry.GTRegistries;

import com.lowdragmc.lowdraglib.LDLib;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.RegistryOps;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public interface IContentSerializer<T> {

    default void toNetwork(FriendlyByteBuf buf, T content) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        buf.writeUtf(codec().encodeStart(ops, content).getOrThrow(false, GTCEu.LOGGER::error).toString());
    }

    default T fromNetwork(FriendlyByteBuf buf) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, GTRegistries.builtinRegistry());
        return codec().parse(ops, LDLib.GSON.fromJson(buf.readUtf(), JsonElement.class)).getOrThrow(false,
                GTCEu.LOGGER::error);
    }

    T fromJson(JsonElement json);

    JsonElement toJson(T content);

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

    Codec<T> codec();

    default T fromJson(JsonElement json, HolderLookup.Provider provider) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        return codec().parse(ops, json).getOrThrow(false, GTCEu.LOGGER::error);
    }

    default JsonElement toJson(T content, HolderLookup.Provider provider) {
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, provider);
        return codec().encodeStart(ops, content).getOrThrow(false, GTCEu.LOGGER::error);
    }

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
        return JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, this.toJson(content));
    }

    default Tag toNbtGeneric(Object content) {
        return toNbt((T) content);
    }

    default T fromNbt(Tag tag) {
        var json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag);
        return fromJson(json);
    }
}
