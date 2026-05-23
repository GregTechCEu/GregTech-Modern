package com.gregtechceu.gtceu.common.datafixer.schemas;

import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.datafixer.schemas.V0.*;

public class V1 extends NamespacedSchema {

    public V1(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        map.remove("steam_miner");
        registerSimpleSteamMachine(schema, map, "steam_miner");

        return map;
    }
}
