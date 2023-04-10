/*
package com.gregtechceu.gtceu.core;

import com.gregtechceu.gtceu.core.IMappedRegistryMixin;
import com.gregtechceu.gtceu.core.mixins.IRegistryAccessor;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Supplier;

public interface IRegistryMixin {

    static <T, R extends WritableRegistry<T>> R internalRegisterToId(ResourceKey<? extends Registry<T>> registryKey, R registry, Registry.RegistryBootstrap<T> loader, Lifecycle lifecycle, int id) {
        ResourceLocation resourceLocation = registryKey.location();
        LinkedHashMap<ResourceLocation, Supplier<?>> map = (LinkedHashMap<ResourceLocation, Supplier<?>>)((HashMap<ResourceLocation, Supplier<?>>) IRegistryAccessor.getLoaders()).clone();
        IRegistryAccessor.getLoaders().clear();

        ResourceLocation[] keys = map.keySet().toArray(ResourceLocation[]::new);
        Supplier<?>[] values = map.values().toArray(Supplier<?>[]::new);

        for (int i = 0; i < id - 1; ++i) {
            IRegistryAccessor.getLoaders().put(keys[i], values[i]);
        }
        IRegistryAccessor.getLoaders().put(resourceLocation, () -> loader.run(registry));
        for (int i = id + 1; i < keys.length; ++i) {
            IRegistryAccessor.getLoaders().put(keys[i], values[i]);
        }

        ((IMappedRegistryMixin<R>)IRegistryAccessor.getWritableRegistry()).registerMappingPushValues(id, (ResourceKey) registryKey, registry, lifecycle, false);
        return registry;
    }
}
*/