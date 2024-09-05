package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public class V2 extends BaseGTSchema {

    public V2(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        TypeTemplate covers = DSL.field("Covers", DSL.list(
                DSL.fields(
                        "side", DSL.byteType().template(),
                        GTReferences.COVER.in(schema))));
        final TypeTemplate pipe = DSL.and(
                DSL.fields(
                        "connectionMask", DSL.byteType().template(),
                        "renderMask", DSL.byteType().template(),
                        "coverHolder", covers),
                DSL.fields(
                        "blockedMask", DSL.byteType().template(),
                        "paintingColor", DSL.intType().template(),
                        "frameMaterial", GTReferences.MATERIAL_NAME.in(schema)));

        schema.register(map, "gtceu:pipe", () -> pipe);
        schema.register(map,
                "gtceu:activable_pipe",
                () -> DSL.fields(
                        "active", DSL.bool().template(),
                        pipe));
        schema.register(map, "gtceu:material_pipe", () -> pipe);
        return map;
    }
}
