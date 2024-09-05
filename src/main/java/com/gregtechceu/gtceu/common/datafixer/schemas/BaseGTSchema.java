package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.common.data.datafixer.GTReferences;

import net.minecraft.util.datafix.schemas.NamespacedSchema;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public class BaseGTSchema extends NamespacedSchema {

    public BaseGTSchema(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
                              Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(false, GTReferences.MATERIAL_NAME, () -> DSL.constType(namespacedString()));
        schema.registerType(false, GTReferences.COVER_NAME, () -> DSL.constType(namespacedString()));

        Map<String, Supplier<TypeTemplate>> coverTypes = registerCoverDefinitions(schema);
        schema.registerType(true, GTReferences.COVER,
                () -> DSL.taggedChoiceLazy("id", namespacedString(), coverTypes));
    }

    public Map<String, Supplier<TypeTemplate>> registerCoverDefinitions(final Schema schema) {
        return Maps.newHashMap();
    }
}
