package com.gregtechceu.gtceu.api.recipe.content;

import com.lowdragmc.lowdraglib.LDLib;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import com.google.gson.JsonElement;
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

    default Tag toNbt(T content, HolderLookup.Provider provider) {
        return JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, this.toJson(content, provider));
    }

    default Tag toNbtGeneric(Object content, HolderLookup.Provider provider) {
        return toNbt((T) content, provider);
    }

    default T fromNbt(Tag tag, HolderLookup.Provider provider) {
        var json = NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag);
        return fromJson(json, provider);
    }
}
