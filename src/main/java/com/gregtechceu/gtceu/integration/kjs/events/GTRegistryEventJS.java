package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryInfo;
import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;

import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;

public class GTRegistryEventJS<K, V> extends StartupEventJS {

    private final GTRegistryInfo<K, V> registry;

    public GTRegistryEventJS(GTRegistryInfo<K, V> r) {
        registry = r;
    }

    public BuilderBase<? extends V> create(GTResourceLocation id, String type) {
        var t = registry.types.get(type);

        if (t == null) {
            throw new IllegalArgumentException("Unknown type '" + type + "' for object '" + id + "'!");
        }

        var b = t.factory()
                .createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, id.wrapped()));

        if (b == null) {
            throw new IllegalArgumentException("Unknown type '" + type + "' for object '" + id + "'!");
        } else {
            registry.addBuilder(b);
        }

        return b;
    }

    public BuilderBase<? extends V> create(GTResourceLocation id) {
        var t = registry.getDefaultType();

        if (t == null) {
            throw new IllegalArgumentException(
                    "Registry for type '" + registry.registryKey + "' doesn't have any builders registered!");
        }

        var b = t.factory()
                .createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, id.wrapped()));

        if (b == null) {
            throw new IllegalArgumentException("Unknown type '" + t.type() + "' for object '" + id + "'!");
        } else {
            registry.addBuilder(b);
        }

        return b;
    }
}
