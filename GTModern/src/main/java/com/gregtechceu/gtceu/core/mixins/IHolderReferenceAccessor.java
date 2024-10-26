package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.Holder;

import org.spongepowered.asm.mixin.Mixin;

@Mixin(Holder.Reference.class)
public interface IHolderReferenceAccessor<T> extends Holder<T> {

    // @Invoker
    // void invokeBind(ResourceKey<T> key, T value);
}
