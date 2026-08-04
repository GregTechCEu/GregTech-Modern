package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.core.util.extensions.IntrinsicTagAppenderExt;

import net.minecraft.data.tags.IntrinsicHolderTagsProvider;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(IntrinsicHolderTagsProvider.IntrinsicTagAppender.class)
public abstract class IntrinsicTagAppenderMixin<T> implements IntrinsicTagAppenderExt<T> {}
