package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.nbt.Tag;

import java.util.function.BiFunction;
import java.util.function.Function;

public class SimpleClassTransformer<T, TagType extends Tag> implements ValueTransformer<T> {

    private final BiFunction<T, ValueTransformer.TransformerContext<T>, TagType> write;
    private final BiFunction<TagType, ValueTransformer.TransformerContext<T>, T> read;
    private final Class<TagType> tagClass;

    public SimpleClassTransformer(Function<T, TagType> write,
                                  Function<TagType, T> read,
                                  Class<TagType> tagClass) {
        this.write = (t, c) -> write.apply(t);
        this.read = (t, c) -> read.apply(t);
        this.tagClass = tagClass;
    }

    public SimpleClassTransformer(BiFunction<T, ValueTransformer.TransformerContext<T>, TagType> write,
                                  BiFunction<TagType, ValueTransformer.TransformerContext<T>, T> read,
                                  Class<TagType> tagClass) {
        this.write = write;
        this.read = read;
        this.tagClass = tagClass;
    }

    @Override
    public Tag serializeNBT(T value, ValueTransformer.TransformerContext<T> context) {
        return write.apply(value, context);
    }

    @Override
    public T deserializeNBT(Tag tag, ValueTransformer.TransformerContext<T> context) {
        TagType t = ValueTransformer.assertTagType(tagClass, tag, context);
        return read.apply(t, context);
    }
}
