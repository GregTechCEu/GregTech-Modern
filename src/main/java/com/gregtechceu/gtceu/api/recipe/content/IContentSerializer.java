package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;

import com.lowdragmc.lowdraglib.LDLib;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.*;

public interface IContentSerializer<T> {

    default void toNetwork(RegistryFriendlyByteBuf buf, T content) {
        buf.writeUtf(codec().encodeStart(
                buf.registryAccess().createSerializationContext(JsonOps.INSTANCE), content).getOrThrow().toString());
    }

    default T fromNetwork(RegistryFriendlyByteBuf buf) {
        return codec().parse(buf.registryAccess().createSerializationContext(JsonOps.INSTANCE),
                LDLib.GSON.fromJson(buf.readUtf(), JsonElement.class)).getOrThrow();
    }

    Codec<T> codec();

    default T fromJson(JsonElement json, HolderLookup.Provider provider) {
        return codec().parse(provider.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
    }

    default JsonElement toJson(T content, HolderLookup.Provider provider) {
        return codec().encodeStart(
                provider.createSerializationContext(JsonOps.INSTANCE), content).getOrThrow();
    }

    T of(Object o);

    T defaultValue();

    @SuppressWarnings("unchecked")
    default void toNetworkContent(RegistryFriendlyByteBuf buf, Content content) {
        T inner = (T) content.getContent();
        toNetwork(buf, inner);
        buf.writeVarInt(content.chance);
        buf.writeVarInt(content.maxChance);
        buf.writeVarInt(content.tierChanceBoost);
        buf.writeBoolean(content.slotName != null);
        if (content.slotName != null) {
            buf.writeUtf(content.slotName);
        }
        buf.writeBoolean(content.uiName != null);
        if (content.uiName != null) {
            buf.writeUtf(content.uiName);
        }
    }

    default Content fromNetworkContent(RegistryFriendlyByteBuf buf) {
        T inner = fromNetwork(buf);
        int chance = buf.readVarInt();
        int maxChance = buf.readVarInt();
        int tierChanceBoost = buf.readVarInt();
        String slotName = null;
        if (buf.readBoolean()) {
            slotName = buf.readUtf();
        }
        String uiName = null;
        if (buf.readBoolean()) {
            uiName = buf.readUtf();
        }
        return new Content(inner, chance, maxChance, tierChanceBoost, slotName, uiName);
    }

    @SuppressWarnings("unchecked")
    default JsonElement toJsonContent(Content content, HolderLookup.Provider provider) {
        JsonObject json = new JsonObject();
        json.add("content", toJson((T) content.getContent(), provider));
        json.addProperty("chance", content.chance);
        json.addProperty("maxChance", content.maxChance);
        json.addProperty("tierChanceBoost", content.tierChanceBoost);
        if (content.slotName != null)
            json.addProperty("slotName", content.slotName);
        if (content.uiName != null)
            json.addProperty("uiName", content.uiName);
        return json;
    }

    default Content fromJsonContent(JsonElement json, HolderLookup.Provider provider) {
        JsonObject jsonObject = json.getAsJsonObject();
        T inner = fromJson(jsonObject.get("content"));
        int chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsInt() : ChanceLogic.getMaxChancedValue();
        int maxChance = jsonObject.has("maxChance") ? jsonObject.get("maxChance").getAsInt() :
                ChanceLogic.getMaxChancedValue();
        int tierChanceBoost = jsonObject.has("tierChanceBoost") ? jsonObject.get("tierChanceBoost").getAsInt() : 0;
        String slotName = jsonObject.has("slotName") ? jsonObject.get("slotName").getAsString() : null;
        String uiName = jsonObject.has("uiName") ? jsonObject.get("uiName").getAsString() : null;
        return new Content(inner, chance, maxChance, tierChanceBoost, slotName, uiName);
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
