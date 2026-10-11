package com.gregtechceu.gtceu.api.datafixer.schemas;

import com.gregtechceu.gtceu.common.datafixer.schemas.V0;

import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AutomaticNamespacedSchema extends NamespacedSchema {

    private String currentAutomaticNamespace;

    public AutomaticNamespacedSchema(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    protected abstract String getAutomaticNamespace();

    private String getCurrentAutomaticNamespace() {
        if (this.currentAutomaticNamespace == null) {
            this.currentAutomaticNamespace = getAutomaticNamespace();
        }
        return this.currentAutomaticNamespace;
    }

    @Override
    public void register(final Map<String, Supplier<TypeTemplate>> map, String name,
                         final Supplier<TypeTemplate> template) {
        if (name.indexOf(':') == -1) {
            name = getCurrentAutomaticNamespace() + ":" + name;
        } else if (name.indexOf(':') == 0) {
            // protect against typos/copy-paste errors
            name = getCurrentAutomaticNamespace() + name;
        }
        super.register(map, name, template);
    }

    /**
     * Register block entity types to the schema by overriding this and calling {@link #register} (and {@link #remove}).
     * There are also a variety of utility functions in {@linkplain Schema vanilla's} and {@linkplain V0 GT's} schemas
     * you may use.
     *
     * <p>
     * Warning: Do not modify the returned map directly. If you do, the values you put in it will default to the
     * {@code minecraft} namespace, which is most likely not what you want. Use the aforementioned utility functions
     * instead.
     */
    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        String originalNamespace = this.getAutomaticNamespace();
        try {
            // switch the namespace to the parent's one automatically
            if (parent instanceof AutomaticNamespacedSchema autoNamespace) {
                this.currentAutomaticNamespace = autoNamespace.getCurrentAutomaticNamespace();
            } else {
                this.currentAutomaticNamespace = "minecraft";
            }
            return super.registerBlockEntities(schema);
        } finally {
            // after processing vanilla things, go back to the assigned namespace
            this.currentAutomaticNamespace = originalNamespace;
        }
    }

    /**
     * Register block entity types to the schema by overriding this and calling {@link #register} (and {@link #remove}).
     * There are also a variety of utility functions in {@linkplain Schema vanilla's} and {@linkplain V0 GT's} schemas
     * you may use.
     *
     * <p>
     * Warning: Do not modify the returned map directly. If you do, the values you put in it will default to the
     * {@code minecraft} namespace, which is most likely not what you want. Use the aforementioned utility functions
     * instead.
     */
    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        String originalNamespace = this.getAutomaticNamespace();
        try {
            // switch the namespace to the parent's one automatically
            if (parent instanceof AutomaticNamespacedSchema autoNamespace) {
                this.currentAutomaticNamespace = autoNamespace.getCurrentAutomaticNamespace();
            } else {
                this.currentAutomaticNamespace = "minecraft";
            }
            return super.registerEntities(schema);
        } finally {
            // after processing vanilla things, go back to the assigned namespace
            this.currentAutomaticNamespace = originalNamespace;
        }
    }

    public void remove(final Map<String, Supplier<TypeTemplate>> map, String name) {
        if (name.indexOf(':') == -1) {
            name = getCurrentAutomaticNamespace() + ":" + name;
        } else if (name.indexOf(':') == 0) {
            // protect against typos/copy-paste errors
            name = getCurrentAutomaticNamespace() + name;
        }
        map.remove(name);
    }
}
