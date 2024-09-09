package com.gregtechceu.gtceu.common.datafixer.fixes;

import net.minecraft.util.datafix.fixes.NamedEntityFix;
import net.minecraft.util.datafix.fixes.References;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class PipeConnectionFix extends NamedEntityFix {

    public PipeConnectionFix(Schema outputSchema, boolean changesType, String type) {
        super(outputSchema, changesType, "PipeConnectionFix", References.BLOCK_ENTITY, type);
    }

    public Dynamic<?> fixTag(Dynamic<?> tag) {
        tag = tag.set("connectionMask", tag.get("connections").result().get());
        return tag.set("blockedMask", tag.get("blockedConnections").result().get());
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), this::fixTag);
    }
}
