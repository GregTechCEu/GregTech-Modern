package com.gregtechceu.gtceu.core.mixins.kjs;

import dev.latvian.mods.kubejs.registry.BuilderBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = BuilderBase.class, remap = false)
public interface BuilderBaseAccessor<T> {

    @Invoker("createTransformedObject")
    T gtceu$createTransformedObject();
}
