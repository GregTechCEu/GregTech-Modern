package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.Nullable;

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

    @Override
    public void writeToPacket(FriendlyByteBuf buf, T value, TransformerContext<T> context) {
        Tag data = codec.encodeStart(context.nbtOps(), value).getOrThrow(false, GTCEu.LOGGER::error);
        if (data instanceof CompoundTag compoundTag) buf.writeNbt(compoundTag);
        else {
            GTCEu.LOGGER.error("Sync: Cannot write non-compound NBT tag to packet. Field {}, class {}", context.fieldName(), context.type().getClassValue());
        }
    }

    @Override
    public @Nullable T readFromPacket(FriendlyByteBuf buf, TransformerContext<T> context) {
        return codec.parse(context.nbtOps(), buf.readNbt()).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
