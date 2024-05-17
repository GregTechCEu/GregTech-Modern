package com.gregtechceu.gtceu.api.recipe.content;

import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.*;

public interface IContentSerializer<T> {

    default void toNetwork(RegistryFriendlyByteBuf buf, T content) {
        buf.writeUtf(LDLib.GSON.toJson(toJson(content, buf.registryAccess())));
    }

    default T fromNetwork(RegistryFriendlyByteBuf buf) {
        return fromJson(LDLib.GSON.fromJson(buf.readUtf(), JsonElement.class), buf.registryAccess());
    }

    default Codec<T> codec() {
        return Codec.PASSTHROUGH.flatXmap(
                dynamic -> DataResult.success(
                        fromJson(dynamic.convert(JsonOps.INSTANCE).getValue(), Platform.getFrozenRegistry()),
                        Lifecycle.stable()),
                json -> DataResult.success(new Dynamic<>(JsonOps.INSTANCE, toJson(json, Platform.getFrozenRegistry())),
                        Lifecycle.stable()));
    }

    T fromJson(JsonElement json, HolderLookup.Provider provider);

    JsonElement toJson(T content, HolderLookup.Provider provider);

    T of(Object o);

    T defaultValue();

    @SuppressWarnings("unchecked")
    default void toNetworkContent(RegistryFriendlyByteBuf buf, Content content) {
        T inner = (T) content.getContent();
        toNetwork(buf, inner);
        buf.writeFloat(content.chance);
        buf.writeFloat(content.tierChanceBoost);
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
        float chance = buf.readFloat();
        float tierChanceBoost = buf.readFloat();
        String slotName = null;
        if (buf.readBoolean()) {
            slotName = buf.readUtf();
        }
        String uiName = null;
        if (buf.readBoolean()) {
            uiName = buf.readUtf();
        }
        return new Content(inner, chance, tierChanceBoost, slotName, uiName);
    }

    @SuppressWarnings("unchecked")
    default JsonElement toJsonContent(Content content, HolderLookup.Provider provider) {
        JsonObject json = new JsonObject();
        json.add("content", toJson((T) content.getContent(), provider));
        json.addProperty("chance", content.chance);
        json.addProperty("tierChanceBoost", content.tierChanceBoost);
        if (content.slotName != null)
            json.addProperty("slotName", content.slotName);
        if (content.uiName != null)
            json.addProperty("uiName", content.uiName);
        return json;
    }

    default Content fromJsonContent(JsonElement json, HolderLookup.Provider provider) {
        JsonObject jsonObject = json.getAsJsonObject();
        T inner = fromJson(jsonObject.get("content"), provider);
        float chance = jsonObject.has("chance") ? jsonObject.get("chance").getAsFloat() : 1;
        float tierChanceBoost = jsonObject.has("tierChanceBoost") ? jsonObject.get("tierChanceBoost").getAsFloat() : 0;
        String slotName = jsonObject.has("slotName") ? jsonObject.get("slotName").getAsString() : null;
        String uiName = jsonObject.has("uiName") ? jsonObject.get("uiName").getAsString() : null;
        return new Content(inner, chance, tierChanceBoost, slotName, uiName);
    }
}
