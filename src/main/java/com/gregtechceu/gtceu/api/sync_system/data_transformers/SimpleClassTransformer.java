package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.nbt.Tag;

import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SimpleClassTransformer<T, TagType extends Tag> implements ValueTransformer<T> {

    private final Function<T, TagType> write;
    private final Function<TagType, T> read;
    private final Class<TagType> tagClass;

    public SimpleClassTransformer(Function<T, TagType> write,
                                  Function<TagType, T> read,
                                  Class<TagType> tagClass) {
        this.write = write;
        this.read = read;
        this.tagClass = tagClass;
    }

    @Override
    public @NotNull Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return write.apply(value);
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        TagType t = ValueTransformer.assertTagType(tagClass, tag, context);
        return read.apply(t);
    }
}
