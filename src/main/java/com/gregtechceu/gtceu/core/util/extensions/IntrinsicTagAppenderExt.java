package com.gregtechceu.gtceu.core.util.extensions;

import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.extensions.IForgeIntrinsicHolderTagAppender;

import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface IntrinsicTagAppenderExt<T> extends TagAppenderExt<T>, IForgeIntrinsicHolderTagAppender<T> {

    private IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> self() {
        return (IntrinsicHolderTagsProvider.IntrinsicTagAppender<T>) this;
    }

    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(Supplier<? extends T> value) {
        return self().add(value.get());
    }

    @SuppressWarnings("unchecked")
    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(Supplier<? extends T>... values) {
        Stream.of(values).forEach(this::add);
        return self();
    }

    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> add(Collection<? extends Supplier<? extends T>> values) {
        values.forEach(this::add);
        return self();
    }

    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptional(T value) {
        return addOptional(getKey(value));
    }

    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptional(Supplier<? extends T> value) {
        return addOptional(value.get());
    }

    @SuppressWarnings("unchecked")
    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptional(Supplier<? extends T>... values) {
        Stream.of(values).forEach(this::addOptional);
        return self();
    }

    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptional(Collection<? extends Supplier<? extends T>> values) {
        values.forEach(this::addOptional);
        return self();
    }

    @Override
    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptional(ResourceKey<T> key) {
        TagAppenderExt.super.addOptional(key);
        return self();
    }

    @Override
    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addTags(Collection<TagKey<T>> values) {
        TagAppenderExt.super.addTags(values);
        return self();
    }

    @Override
    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptionalTag(TagKey<T> value) {
        self().addOptionalTag(value.location());
        return self();
    }

    @SuppressWarnings("unchecked")
    @Override
    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptionalTags(TagKey<T>... values) {
        TagAppenderExt.super.addOptionalTags(values);
        return self();
    }

    default IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addOptionalTags(Collection<TagKey<T>> values) {
        TagAppenderExt.super.addOptionalTags(values);
        return self();
    }
}
