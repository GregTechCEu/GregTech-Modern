package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

public interface IContentSerializer<T> {

    default void toNetwork(RegistryFriendlyByteBuf buf, T content) {
        buf.writeJsonWithCodec(codec(), content);
    }

    default T fromNetwork(RegistryFriendlyByteBuf buf) {
        return buf.readJsonWithCodec(codec());
    }

    default T fromJson(JsonElement json, HolderLookup.Provider provider) {
        return codec().parse(provider.createSerializationContext(JsonOps.INSTANCE), json).getOrThrow();
    }

    default JsonElement toJson(T content, HolderLookup.Provider provider) {
        return codec().encodeStart(provider.createSerializationContext(JsonOps.INSTANCE), content).getOrThrow();
    }

    T of(Object o);

    T defaultValue();

    @SuppressWarnings("unchecked")
    default void toNetworkContent(RegistryFriendlyByteBuf buf, Content content) {
        T inner = (T) content.content();
        toNetwork(buf, inner);
        buf.writeVarInt(content.chance());
        buf.writeVarInt(content.maxChance());
        buf.writeVarInt(content.tierChanceBoost());
    }

    default Content fromNetworkContent(RegistryFriendlyByteBuf buf) {
        T inner = fromNetwork(buf);
        int chance = buf.readVarInt();
        int maxChance = buf.readVarInt();
        int tierChanceBoost = buf.readVarInt();
        return new Content(inner, chance, maxChance, tierChanceBoost);
    }

    Class<T> contentClass();

    Codec<T> codec();

    default Tag toNbt(T content, HolderLookup.Provider provider) {
        return codec().encodeStart(provider.createSerializationContext(NbtOps.INSTANCE), content).getOrThrow();
    }

    default T fromNbt(Tag tag, HolderLookup.Provider provider) {
        return codec().parse(provider.createSerializationContext(NbtOps.INSTANCE), tag).getOrThrow();
    }
}
