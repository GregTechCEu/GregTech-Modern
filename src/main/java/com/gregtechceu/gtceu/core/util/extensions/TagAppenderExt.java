package com.gregtechceu.gtceu.core.util.extensions;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraftforge.common.extensions.IForgeTagAppender;

import java.util.Collection;

public interface TagAppenderExt<T> extends IForgeTagAppender<T> {

    private TagsProvider.TagAppender<T> self() {
        return (TagsProvider.TagAppender<T>) this;
    }

    default TagsProvider.TagAppender<T> addOptional(ResourceKey<T> key) {
        return self().addOptional(key.location());
    }

    default TagsProvider.TagAppender<T> addTags(Collection<TagKey<T>> values) {
        TagsProvider.TagAppender<T> builder = self();
        values.forEach(builder::addTag);
        return builder;
    }

    default TagsProvider.TagAppender<T> addOptionalTags(Collection<TagKey<T>> values) {
        values.forEach(this::addOptionalTag);
        return self();
    }
}
