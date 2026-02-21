package com.gregtechceu.gtceu.core.mixins.kubejs;

import dev.latvian.mods.rhino.Context;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Context.class, remap = false)
public interface ContextAccessor {

    @Accessor("lastInterpreterFrame")
    Object gtceu$getLastInterpreterFrame();
}
