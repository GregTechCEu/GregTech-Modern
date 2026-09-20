package com.gregtechceu.gtceu.api.recipe.content;

import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;

import net.minecraft.network.FriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public interface IContentSerializer<T> extends RecipeContentCodec<T> {

    T of(Object o);

    T defaultValue();

    @SuppressWarnings("unchecked")
    default void toNetworkContent(FriendlyByteBuf buf, Content content) {
        T inner = (T) content.content();
        toNetwork(buf, inner);
        buf.writeVarInt(content.chance());
        buf.writeVarInt(content.maxChance());
    }

    default Content fromNetworkContent(FriendlyByteBuf buf) {
        T inner = fromNetwork(buf);
        int chance = buf.readVarInt();
        int maxChance = buf.readVarInt();
        return new Content(inner, chance, maxChance);
    }

    Class<T> contentClass();

    @SuppressWarnings("unchecked")
    default JsonElement toJsonContent(Content content) {
        JsonObject json = new JsonObject();
        json.add("content", toJson((T) content.content()));
        json.addProperty("chance", content.chance());
        json.addProperty("maxChance", content.maxChance());
        return json;
    }

    default Content fromJsonContent(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        T inner = fromJson(jsonObject.get("content"));
        int chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsInt() : ChanceLogic.getMaxChancedValue();
        int maxChance = jsonObject.has("maxChance") ? jsonObject.get("maxChance").getAsInt() :
                ChanceLogic.getMaxChancedValue();
        return new Content(inner, chance, maxChance);
    }

}
