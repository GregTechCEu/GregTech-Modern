package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class SimpleClassTransformer<T, TagType extends Tag> implements ValueTransformer<T> {

    private final Function<T, TagType> writeNBT;
    private final Function<TagType, @Nullable T> readNBT;
    private final Class<TagType> tagClass;
    private final BiConsumer<FriendlyByteBuf, T> writePacket;
    private final Function<FriendlyByteBuf, @Nullable T> readPacket;

    public SimpleClassTransformer(Function<T, TagType> writeNBT,
                                  Function<TagType, T> readNBT,
                                  BiConsumer<FriendlyByteBuf, T> writePacket,
                                  Function<FriendlyByteBuf, T> readPacket,
                                  Class<TagType> tagClass) {
        this.writeNBT = writeNBT;
        this.readNBT = readNBT;
        this.writePacket = writePacket;
        this.readPacket = readPacket;
        this.tagClass = tagClass;
    }

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return writeNBT.apply(value);
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        TagType t = ValueTransformer.assertTagType(tagClass, tag, context);
        return readNBT.apply(t);
    }

    @Override
    public void writeToPacket(FriendlyByteBuf buf, T value, TransformerContext<T> context) {
        writePacket.accept(buf, value);
    }

    @Override
    public @Nullable T readFromPacket(FriendlyByteBuf buf, TransformerContext<T> context) {
        return readPacket.apply(buf);
    }
}
