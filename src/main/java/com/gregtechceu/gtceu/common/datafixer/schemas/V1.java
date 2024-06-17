package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.fixes.References;

import java.util.*;
import java.util.function.Supplier;

public class V1 extends Schema {

    public V1(int versionKey, Schema parent) {
        super(versionKey, parent);
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
        schema.register(map, name, () -> DSL.optionalFields(
                "importItems",
                DSL.optionalFields("storage",
                        DSL.optionalFields("Items",
                                DSL.list(References.ITEM_STACK.in(schema)))),
                "exportItems",
                DSL.optionalFields("storage",
                        DSL.optionalFields("Items",
                                DSL.list(References.ITEM_STACK.in(schema)))),
                "inventory",
                DSL.optionalFields("storage",
                        DSL.optionalFields("Items",
                                DSL.list(References.ITEM_STACK.in(schema)))),
                "cache",
                DSL.optionalFields("storage",
                        DSL.optionalFields("Items",
                                DSL.list(References.ITEM_STACK.in(schema))))));
    }
}

