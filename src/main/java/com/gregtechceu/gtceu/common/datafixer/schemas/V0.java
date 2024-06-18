package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.data.datafixer.GTReferences;

import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.*;
import java.util.function.Supplier;

public class V0 extends NamespacedSchema {

    public V0(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
                              Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(false, GTReferences.MATERIAL_NAME, () -> DSL.constType(namespacedString()));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        for (MachineDefinition definition : GTRegistries.MACHINES) {
            registerInventory(schema, map, definition.getId().toString());
        }
        return map;
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> DSL.or(
                DSL.fields(
                        "importItems",
                        DSL.field("storage",
                                DSL.field("Items",
                                        DSL.list(References.ITEM_STACK.in(schema)))),
                        "exportItems",
                        DSL.field("storage",
                                DSL.field("Items",
                                        DSL.list(References.ITEM_STACK.in(schema))))),
                DSL.or(
                        DSL.fields(
                                "inventory",
                                DSL.field("storage",
                                        DSL.field("Items",
                                                DSL.list(References.ITEM_STACK.in(schema))))),
                        DSL.or(
                                DSL.fields(
                                        "cache",
                                        DSL.field("storage",
                                                DSL.field("Items",
                                                        DSL.list(References.ITEM_STACK.in(schema))))),
                                DSL.remainder()))));
    }
}
