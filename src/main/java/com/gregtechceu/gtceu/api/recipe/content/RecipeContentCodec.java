package com.gregtechceu.gtceu.api.recipe.content;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.LenientJsonParser;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;

/** Codec transport independent of GT's machine, chance and UI classes. */
public interface RecipeContentCodec<T> {
    Codec<T> codec();

    default void toNetwork(FriendlyByteBuf buf, T content) {
        if (buf instanceof RegistryFriendlyByteBuf registryBuf) {
            var ops = RegistryOps.create(JsonOps.INSTANCE, registryBuf.registryAccess());
            var json = codec().encodeStart(ops, content).getOrThrow(EncoderException::new);
            buf.writeUtf(json.toString());
        } else {
            buf.writeJsonWithCodec(codec(), content);
        }
    }

    default T fromNetwork(FriendlyByteBuf buf) {
        if (buf instanceof RegistryFriendlyByteBuf registryBuf) {
            var ops = RegistryOps.create(JsonOps.INSTANCE, registryBuf.registryAccess());
            return codec().parse(ops, LenientJsonParser.parse(buf.readUtf())).getOrThrow(DecoderException::new);
        }
        return buf.readLenientJsonWithCodec(codec());
    }

    default T fromJson(JsonElement json) {
        return codec().parse(JsonOps.INSTANCE, json).getOrThrow();
    }

    default T fromJson(JsonElement json, HolderLookup.Provider registries) {
        return codec().parse(RegistryOps.create(JsonOps.INSTANCE, registries), json).getOrThrow();
    }

    default JsonElement toJson(T content) {
        return codec().encodeStart(JsonOps.INSTANCE, content).getOrThrow();
    }

    default JsonElement toJson(T content, HolderLookup.Provider registries) {
        return codec().encodeStart(RegistryOps.create(JsonOps.INSTANCE, registries), content).getOrThrow();
    }

    default Tag toNbt(T content) {
        return codec().encodeStart(NbtOps.INSTANCE, content).getOrThrow();
    }

    default Tag toNbt(T content, HolderLookup.Provider registries) {
        return codec().encodeStart(RegistryOps.create(NbtOps.INSTANCE, registries), content).getOrThrow();
    }

    default T fromNbt(Tag tag) {
        return codec().parse(NbtOps.INSTANCE, tag).getOrThrow();
    }

    default T fromNbt(Tag tag, HolderLookup.Provider registries) {
        return codec().parse(RegistryOps.create(NbtOps.INSTANCE, registries), tag).getOrThrow();
    }
}
