package com.gregtechceu.gtceu.api.registry.registrate.entry;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.neoforged.neoforge.registries.DeferredHolder;

import com.tterrag.registrate.util.entry.RegistryEntry;
import lombok.Getter;

public class PlainEntry<R> extends RegistryEntry<R, R> {

    @Getter
    private final GTRegistrate owner;

    public PlainEntry(GTRegistrate owner, DeferredHolder<R, R> key) {
        super(owner, key);
        this.owner = owner;
    }
}
