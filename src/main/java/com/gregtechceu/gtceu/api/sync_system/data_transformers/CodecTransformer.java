package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;

public record CodecTransformer<T>(Codec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec)
        implements ValueTransformer<T> {

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return codec.encodeStart(context.nbtOps(), value).getOrThrow();
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        return codec.parse(context.nbtOps(), tag).getOrThrow();
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buf, T value, TransformerContext<T> context) {
        streamCodec.encode(buf, value);
    }

    @Override
    public T readFromPacket(RegistryFriendlyByteBuf buf, TransformerContext<T> context) {
        return streamCodec.decode(buf);
    }
}
