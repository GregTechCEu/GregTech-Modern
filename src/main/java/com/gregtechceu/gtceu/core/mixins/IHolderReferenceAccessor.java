package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Holder.Reference.class)
public interface IHolderReferenceAccessor<T> extends Holder<T> {

//    @Invoker
//    void invokeBind(ResourceKey<T> key, T value);
}
