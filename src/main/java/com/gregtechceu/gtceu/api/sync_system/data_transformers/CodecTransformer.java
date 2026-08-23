package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;

public class CodecTransformer<T> implements ValueTransformer<T> {

    private final Codec<T> codec;

    public CodecTransformer(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return codec.encodeStart(context.nbtOps(), value).getOrThrow();
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        return codec.parse(context.nbtOps(), tag).getOrThrow();
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, T value, TransformerContext<T> context) {
        Tag data = codec.encodeStart(context.nbtOps(), value).getOrThrow(false, GTCEu.LOGGER::error);
        if (data instanceof CompoundTag compoundTag) {
            buf.writeNbt(compoundTag);
        } else {
            CompoundTag wrapper = new CompoundTag();
            wrapper.put("$$gtceu:value$$", data);
            buf.writeNbt(wrapper);
        }
    }

    @Override
    public @Nullable T readFromPacket(FriendlyByteBuf buf, TransformerContext<T> context) {
        Tag read = buf.readNbt();
        if (read instanceof CompoundTag compound && compound.size() == 1 && compound.contains("$$gtceu:value$$")) {
            read = compound.get("$$gtceu:value$$");
        }
        return codec.parse(context.nbtOps(), read).getOrThrow(false, GTCEu.LOGGER::error);
    }
}
