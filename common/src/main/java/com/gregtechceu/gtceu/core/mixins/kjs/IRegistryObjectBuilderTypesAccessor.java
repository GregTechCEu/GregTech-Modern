package com.gregtechceu.gtceu.core.mixins.kjs;

import dev.latvian.mods.kubejs.RegistryObjectBuilderTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = RegistryObjectBuilderTypes.class, remap = false)
public interface IRegistryObjectBuilderTypesAccessor {

    @Invoker(remap = false)
    void invokePostEvent();
}
