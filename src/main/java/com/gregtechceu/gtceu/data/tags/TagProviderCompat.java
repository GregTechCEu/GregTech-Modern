package com.gregtechceu.gtceu.data.tags;

import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

final class TagProviderCompat {

    private static final Method TAG_METHOD;
    private static final Method RAW_BUILDER_METHOD;
    private static final Field REGISTRY_KEY_FIELD;

    static {
        try {
            TAG_METHOD = IntrinsicHolderTagsProvider.class.getDeclaredMethod("tag", TagKey.class);
            TAG_METHOD.setAccessible(true);
            RAW_BUILDER_METHOD = TagsProvider.class.getDeclaredMethod("getOrCreateRawBuilder", TagKey.class);
            RAW_BUILDER_METHOD.setAccessible(true);
            REGISTRY_KEY_FIELD = TagsProvider.class.getDeclaredField("registryKey");
            REGISTRY_KEY_FIELD.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private TagProviderCompat() {}

    @SuppressWarnings("unchecked")
    static <T> TagAppender<T, T> tag(IntrinsicHolderTagsProvider<T> provider, TagKey<T> tagKey) {
        try {
            return (TagAppender<T, T>) TAG_METHOD.invoke(provider, tagKey);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create tag appender for " + tagKey, e);
        }
    }

    static <T> TagAppender<ResourceKey<T>, T> rawTag(TagsProvider<T> provider, TagKey<T> tagKey) {
        try {
            var builder = (net.minecraft.tags.TagBuilder) RAW_BUILDER_METHOD.invoke(provider, tagKey);
            return TagAppender.forBuilder(builder);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create raw tag appender for " + tagKey, e);
        }
    }

    @SuppressWarnings("unchecked")
    static <T> ResourceKey<T> key(TagsProvider<T> provider, Identifier id) {
        try {
            var registryKey = (ResourceKey<? extends net.minecraft.core.Registry<T>>) REGISTRY_KEY_FIELD.get(provider);
            return ResourceKey.create(registryKey, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to create registry key for " + id, e);
        }
    }

    static <T> void addOptional(TagsProvider<T> provider, TagKey<T> tagKey, Identifier id) {
        rawTag(provider, tagKey).addOptional(key(provider, id));
    }

    static <T> void addOptionalTag(TagsProvider<T> provider, TagKey<T> tagKey, Identifier id) {
        rawTag(provider, tagKey).addOptionalTag(TagKey.create(key(provider, id).registryKey(), id));
    }
}
