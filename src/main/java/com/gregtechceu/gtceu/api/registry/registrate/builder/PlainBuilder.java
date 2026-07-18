package com.gregtechceu.gtceu.api.registry.registrate.builder;

import com.gregtechceu.gtceu.api.registry.registrate.entry.PlainEntry;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;

public abstract class PlainBuilder<R, P, S extends PlainBuilder<R, P, S>> extends AbstractBuilder<R, R, P, S> {

    public PlainBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<? extends Registry<R>> registryKey) {
        super(owner, parent, name, callback, registryKey);
    }

    @Override
    protected PlainEntry<R> createEntryWrapper(DeferredHolder<R, R> delegate) {
        return new PlainEntry<>(getOwner(), delegate);
    }

    @Override
    public PlainEntry<R> register() {
        return (PlainEntry<R>) super.register();
    }
}
