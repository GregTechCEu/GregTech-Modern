package com.gregtechceu.gtceu.core.mixins.dev.datagen;

import com.gregtechceu.gtceu.core.util.extensions.TagAppenderExt;

import net.minecraft.data.tags.TagsProvider;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(TagsProvider.TagAppender.class)
public abstract class TagAppenderMixin<T> implements TagAppenderExt<T> {}
