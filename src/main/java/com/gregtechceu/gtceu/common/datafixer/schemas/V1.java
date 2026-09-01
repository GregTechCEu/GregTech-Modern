package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.datafixer.schemas.AutomaticNamespacedSchema;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.datafixer.schemas.V0.*;

public class V1 extends AutomaticNamespacedSchema {

    public V1(int versionKey, Schema parent) {
        super(versionKey, parent, GTCEu.MOD_ID);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        remove(map, "steam_miner");
        registerSimpleSteamMachine(schema, map, "steam_miner");

        return map;
    }
}
