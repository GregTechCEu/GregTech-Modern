package com.gregtechceu.gtceu.common.datafixer.schemas;

import com.gregtechceu.gtceu.core.mixins.datafixer.V705Accessor;

import net.minecraft.util.datafix.fixes.References;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

import static com.mojang.datafixers.DSL.*;

public class V1_5 extends AutoGTNamespacedSchema {

    public V1_5(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes,
                              Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);

        // remove part stats NBT field
        schema.registerType(true, References.ITEM_STACK, () -> hook(
                optionalFields(
                        "id", References.ITEM_NAME.in(schema),
                        "tag", optionalFields(
                                "EntityTag", References.ENTITY_TREE.in(schema),
                                "BlockEntityTag", References.BLOCK_ENTITY.in(schema),
                                "CanDestroy", list(References.BLOCK_NAME.in(schema)),
                                "CanPlaceOn", list(References.BLOCK_NAME.in(schema)),
                                "Items", list(References.ITEM_STACK.in(schema)))),
                V705Accessor.gtceu$getAddNames(), Hook.HookFunction.IDENTITY));
    }
}
