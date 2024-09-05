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
        final Supplier<TypeTemplate> covers = () -> DSL.field("covers", DSL.list(
                DSL.fields(
                        "side", DSL.byteType().template(),
                        GTReferences.COVER.in(schema))));
        final Supplier<TypeTemplate> pipe = () -> DSL.allWithRemainder(
                DSL.fields(
                        "connectionMask", DSL.byteType().template(),
                        "renderMask", DSL.byteType().template(),
                        "coverHolder", covers.get()),
                DSL.fields(
                        "blockedMask", DSL.byteType().template(),
                        "paintingColor", DSL.intType().template(),
                        "frameMaterial", GTReferences.MATERIAL_NAME.in(schema)));

        schema.register(map, "gtceu:pipe", pipe);
        schema.register(map, "gtceu:activable_pipe",
                () -> DSL.and(
                        DSL.optionalFields("active", DSL.bool().template()),
                        pipe.get()));
        schema.register(map, "gtceu:material_pipe", pipe);
        return map;
    }
}
