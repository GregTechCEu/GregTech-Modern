package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.GTCEu;

import com.gregtechceu.gtceu.integration.kjs.helpers.GTResourceLocation;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import dev.latvian.mods.kubejs.registry.RegistryInfo;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;

import java.util.LinkedList;
import java.util.List;

public class GTRegistryEventJS<T> extends StartupEventJS {

    private final RegistryInfo<T> registry;
    public final List<BuilderBase<? extends T>> created;

    public GTRegistryEventJS(RegistryInfo<T> r) {
        this.registry = r;
        this.created = new LinkedList<>();
    }

    public BuilderBase<? extends T> create(GTResourceLocation id, String type) {
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

    public BuilderBase<? extends T> create(GTResourceLocation id) {
        var t = registry.getDefaultType();

        if (t == null) {
            throw new IllegalArgumentException(
                    "Registry for type '" + registry.key + "' doesn't have any builders registered!");
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
