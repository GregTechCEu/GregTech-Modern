package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

import java.util.Map;
import java.util.function.Supplier;

import static com.gregtechceu.gtceu.common.data.machines.GTMachineUtils.ELECTRIC_TIERS;
import static com.gregtechceu.gtceu.common.datafixer.schemas.V1_3.registerSimpleMachine;

public class V1_4 extends NamespacedSchema {

    public V1_4(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);

        registerSimpleMachine(schema, map, "gtceu:welder", ELECTRIC_TIERS);
        registerSimpleMachine(schema, map, "gtceu:spooler", ELECTRIC_TIERS);

        return map;
    }
}
