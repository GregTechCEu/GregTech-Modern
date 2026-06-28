package com.gregtechceu.gtceu.api.registry.registrate.holder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryObject;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;

public abstract class HolderBuilder<R, T extends R, P, S extends HolderBuilder<R, T, P, S>> extends AbstractBuilder<R, T, P, S> {

    public HolderBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                         ResourceKey<Registry<R>> registryKey) {
        super(owner, parent, name, callback, registryKey);
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
