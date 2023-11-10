package com.gregtechceu.gtceu.integration.kjs.events;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.registry.registrate.BuilderBase;
import com.gregtechceu.gtceu.integration.kjs.GTRegistryObjectBuilderTypes;
import dev.latvian.mods.kubejs.event.StartupEventJS;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.kubejs.util.UtilsJS;

import java.util.stream.Stream;

public class GTRegistryEventJS<K, V> extends StartupEventJS {
    private final GTRegistryObjectBuilderTypes<K, V> registry;

    public GTRegistryEventJS(GTRegistryObjectBuilderTypes<K, V> r) {
        registry = r;
    }

    public BuilderBase<? extends V> create(String id, String type) {
        var t = registry.types.get(type);

        if (t == null) {
            throw new IllegalArgumentException("Unknown type '" + type + "' for object '" + id + "'!");
        }

        var b = t.factory().createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, GTCEu.appendId(id)));

        if (b == null) {
            throw new IllegalArgumentException("Unknown type '" + type + "' for object '" + id + "'!");
        } else {
            registry.addBuilder(b);
        }

        return b;
    }

    public BuilderBase<? extends V> create(String id, String type, Object... args) {
        var t = registry.types.get(type);

        if (t == null) {
            return create(id, Stream.of(type, args).flatMap(arg -> arg instanceof Object[] array ? Stream.of(array) : Stream.of(arg)).map(Object.class::cast).toArray());
        }

        var b = t.factory().createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, GTCEu.appendId(id)), args);

        if (b == null) {
            throw new IllegalArgumentException("Unknown type '" + type + "' for object '" + id + "'!");
        } else {
            registry.addBuilder(b);
        }

        return b;
    }

    public BuilderBase<? extends V> create(String id) {
        var t = registry.getDefaultType();

        if (t == null) {
            throw new IllegalArgumentException("Registry for type '" + registry.registryKey + "' doesn't have any builders registered!");
        }

        var b = t.factory().createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, GTCEu.appendId(id)));

        if (b == null) {
            throw new IllegalArgumentException("Unknown type '" + t.type() + "' for object '" + id + "'!");
        } else {
            registry.addBuilder(b);
        }

        return b;
    }

    public BuilderBase<? extends V> create(String id, Object... args) {
        var t = registry.getDefaultType();

        if (t == null) {
            throw new IllegalArgumentException("Registry for type '" + registry.registryKey + "' doesn't have any builders registered!");
        }

        var b = t.factory().createBuilder(UtilsJS.getMCID(ScriptType.STARTUP.manager.get().context, GTCEu.appendIdString(id)), args);

        if (b == null) {
            throw new IllegalArgumentException("Unknown type '" + t.type() + "' for object '" + id + "'!");
        } else {
            registry.addBuilder(b);
        }

        return b;
    }
}
