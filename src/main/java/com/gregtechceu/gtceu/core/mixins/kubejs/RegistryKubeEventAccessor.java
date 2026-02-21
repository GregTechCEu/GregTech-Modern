package com.gregtechceu.gtceu.core.mixins.kubejs;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import dev.latvian.mods.kubejs.registry.RegistryKubeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RegistryKubeEvent.class, remap = false)
public interface RegistryKubeEventAccessor<T> {

    @Accessor("registryKey")
    ResourceKey<Registry<T>> gtceu$getRegistryKey();
}
