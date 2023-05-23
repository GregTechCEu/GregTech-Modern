package com.gregtechceu.gtceu.api.recipe.content;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.gregtechceu.gtlib.GTLib;
import net.minecraft.network.FriendlyByteBuf;

public interface IContentSerializer<T> {

    default void toNetwork(FriendlyByteBuf buf, T content) {
        buf.writeUtf(GTLib.GSON.toJson(toJson(content)));
    }

    default T fromNetwork(FriendlyByteBuf buf) {
        return fromJson(GTLib.GSON.fromJson(buf.readUtf(), JsonElement.class));
    }

    T fromJson(JsonElement json);

    JsonElement toJson(T content);

    T of(Object o);


    @SuppressWarnings("unchecked")
    default void toNetworkContent(FriendlyByteBuf buf, Content content) {
        T inner = (T) content.getContent();
        toNetwork(buf, inner);
        buf.writeFloat(content.chance);
        buf.writeBoolean(content.slotName != null);
        if (content.slotName != null) {
            buf.writeUtf(content.slotName);
        }
        buf.writeBoolean(content.uiName != null);
        if (content.uiName != null) {
            buf.writeUtf(content.uiName);
        }
    }

    default Content fromNetworkContent(FriendlyByteBuf buf) {
        T inner = fromNetwork(buf);
        float chance = buf.readFloat();
        String slotName = null;
        if (buf.readBoolean()) {
            slotName = buf.readUtf();
        }
        String uiName = null;
        if (buf.readBoolean()) {
            uiName = buf.readUtf();
        }
        return new Content(inner, chance, slotName, uiName);
    }

    @SuppressWarnings("unchecked")
    default JsonElement toJsonContent(Content content) {
        JsonObject json = new JsonObject();
        json.add("content", toJson((T) content.getContent()));
        json.addProperty("chance", content.chance);
        if (content.slotName != null)
            json.addProperty("slotName", content.slotName);
        if (content.uiName != null)
            json.addProperty("uiName", content.uiName);
        return json;
    }

    default Content fromJsonContent(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        T inner = fromJson(jsonObject.get("content"));
        float chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsFloat() : 1;
        String slotName = jsonObject.has("slotName") ? jsonObject.get("slotName").getAsString() : null;
        String uiName = jsonObject.has("uiName") ? jsonObject.get("uiName").getAsString() : null;
        return new Content(inner, chance, slotName, uiName);
    }
}
