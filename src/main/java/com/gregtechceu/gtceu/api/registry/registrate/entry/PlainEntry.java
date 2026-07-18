package com.gregtechceu.gtceu.api.registry.registrate.entry;

import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class PlainEntry<R> extends RegistryEntry<R, R> {

    public PlainEntry(AbstractRegistrate<?> owner, DeferredHolder<R, R> key) {
        super(owner, key);
    }
}
