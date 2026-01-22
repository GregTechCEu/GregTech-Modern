package com.gregtechceu.gtceu.syncsystem.data_transformers;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.mojang.serialization.Codec;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

public class CodecTransformer<T> extends ValueTransformer<T> {
    private final Codec<T> codec;

    public CodecTransformer(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public Tag serializeNBT(T value, ISyncManaged holder) {
        return codec.encodeStart(NbtOps.INSTANCE, value).getOrThrow(false, GTCEu.LOGGER::error);
    }

    @Override
    public T deserializeNBT(Tag tag, ISyncManaged holder, @Nullable T currentVal) {
        return codec.parse(NbtOps.INSTANCE, tag).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
