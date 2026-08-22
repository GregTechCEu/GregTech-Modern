package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.Tag;

import com.mojang.serialization.Codec;

public class CodecTransformer<T> implements ValueTransformer<T> {

    private final Codec<T> codec;

    public CodecTransformer(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return codec.encodeStart(context.nbtOps(), value).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        return codec.parse(context.nbtOps(), tag).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
