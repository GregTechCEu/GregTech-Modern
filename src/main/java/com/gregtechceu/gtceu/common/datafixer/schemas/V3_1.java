package com.gregtechceu.gtceu.common.datafixer.schemas;

import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public class V3_1 extends NamespacedSchema {

    public V3_1(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        schema.register(map, "gtceu:rotary_hearth_furnace", map.remove("gtceu:mega_blast_furnace"));
        schema.register(map, "gtceu:bulk_blast_chiller", map.remove("gtceu:mega_vacuum_freezer"));

        return map;
    }
}
