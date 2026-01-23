package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import com.mojang.serialization.Codec;

public class CodecTransformer<T> extends ValueTransformer<T> {

    private final Codec<T> codec;

    public CodecTransformer(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        return codec.parse(NbtOps.INSTANCE, tag).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
