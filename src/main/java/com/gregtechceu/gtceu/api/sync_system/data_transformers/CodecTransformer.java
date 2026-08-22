package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import com.mojang.serialization.Codec;
import net.minecraft.resources.RegistryOps;
import org.jetbrains.annotations.Nullable;

public class CodecTransformer<T> implements ValueTransformer<T> {

    private final Codec<T> codec;
    private @Nullable RegistryOps<Tag> ops;

    public CodecTransformer(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        if (ops == null) ops = context.lookup().createSerializationContext(NbtOps.INSTANCE);
        return codec.encodeStart(ops, value).getOrThrow();
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        if (ops == null) ops = context.lookup().createSerializationContext(NbtOps.INSTANCE);
        return codec.parse(ops, tag).getOrThrow();
    }
}
