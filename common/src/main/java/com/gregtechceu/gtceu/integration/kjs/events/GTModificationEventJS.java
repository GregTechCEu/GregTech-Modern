package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.integration.kjs.GTRegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.StartupEventJS;

public class GTModificationEventJS<K, V> extends StartupEventJS {
    private final GTRegistryObjectBuilderTypes<K, V> registry;

    public GTModificationEventJS(GTRegistryObjectBuilderTypes<K, V> r) {
        registry = r;
    }

    public V modify(K id) {
        V value;
        value = registry.registryValues.get(id);

        if (value == null) {
            throw new IllegalArgumentException("Unknown registry key '" + id + "' for object '" + id + "'!");
        }

        return value;
    }
}
