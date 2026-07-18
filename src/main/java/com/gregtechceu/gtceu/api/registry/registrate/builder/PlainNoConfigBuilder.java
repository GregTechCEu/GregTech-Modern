package com.gregtechceu.gtceu.api.registry.registrate.builder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public class PlainNoConfigBuilder<R, P> extends PlainBuilder<R, P, PlainNoConfigBuilder<R, P>> {

    private final NonNullSupplier<R> factory;

    public PlainNoConfigBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                ResourceKey<? extends Registry<R>> registryType, NonNullSupplier<R> factory) {
        super(owner, parent, name, callback, registryType);
        this.factory = factory;
    }

    @Override
    protected R createEntry() {
        return this.factory.get();
    }
}
