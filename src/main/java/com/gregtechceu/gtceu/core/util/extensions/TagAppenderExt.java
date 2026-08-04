package com.gregtechceu.gtceu.core.util.extensions;

import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.extensions.IForgeTagAppender;

public interface TagAppenderExt<T> extends IForgeTagAppender<T> {

    private TagsProvider.TagAppender<T> self() {
        return (TagsProvider.TagAppender<T>) this;
    }

    default TagsProvider.TagAppender<T> addOptional(ResourceKey<T> key) {
        return self().addOptional(key.location());
    }
}
