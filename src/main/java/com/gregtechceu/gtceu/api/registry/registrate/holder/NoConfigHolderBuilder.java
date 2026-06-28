package com.gregtechceu.gtceu.api.registry.registrate.holder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryObject;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public class NoConfigHolderBuilder<R, T extends R, P> extends NoConfigBuilder<R, T, P> {

    public NoConfigHolderBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                 ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        super(owner, parent, name, callback, registryType, factory);
    }

    @Override
    protected HolderRegistryEntry<T> createEntryWrapper(RegistryObject<T> delegate) {
        return new HolderRegistryEntry<>(getOwner(), delegate);
    }

    @Override
    public HolderRegistryEntry<T> register() {
        return (HolderRegistryEntry<T>) super.register();
    }

    @Override
    public HolderRegistryEntry<T> get() {
        return (HolderRegistryEntry<T>) super.get();
    }
}