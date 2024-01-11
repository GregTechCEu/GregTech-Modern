package com.gregtechceu.gtceu.core.mixins.ae2;

import appeng.helpers.externalstorage.GenericStackInv;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GenericStackInv.class, remap = false)
public interface GenericStackInvAccessor {

    @Accessor
    Runnable getListener();

    @Accessor @Mutable
    void setListener(Runnable listener);
}
