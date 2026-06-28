package com.gregtechceu.gtceu.api.registry.registrate.holder;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

public class NoConfigHolderBuilder<R, T extends R, P> extends HolderBuilder<R, T, P, NoConfigHolderBuilder<R, T, P>> {

    private final NonNullSupplier<T> factory;

    public NoConfigHolderBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                 ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        super(owner, parent, name, callback, registryType);
        this.factory = factory;
    }

    @Override
    protected @NonnullType T createEntry() {
        return factory.get();
    }
}