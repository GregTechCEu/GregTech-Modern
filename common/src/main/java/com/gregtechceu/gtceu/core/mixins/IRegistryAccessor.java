package com.gregtechceu.gtceu.core.mixins;

import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.NotImplementedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.function.Supplier;

@Mixin(Registry.class)
public interface IRegistryAccessor<T> extends Keyable, IdMap<T> {

    @Accessor("LOADERS")
    static Map<ResourceLocation, Supplier<?>> getLoaders() {
        throw new NotImplementedException("Mixin failed to apply");
    }

    @Accessor("WRITABLE_REGISTRY")
    static WritableRegistry<WritableRegistry<?>> getWritableRegistry() {
        throw new NotImplementedException("Mixin failed to apply");
    }

    @Invoker
    static <T, R extends WritableRegistry<T>> R invokeInternalRegister(ResourceKey<? extends Registry<T>> registryKey, R registry, Registry.RegistryBootstrap<T> loader, Lifecycle lifecycle) {
        throw new NotImplementedException("Mixin failed to apply");
    }
}
