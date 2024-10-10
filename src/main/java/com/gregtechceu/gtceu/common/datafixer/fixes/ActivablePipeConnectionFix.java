package com.gregtechceu.gtceu.common.datafixer.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class ActivablePipeConnectionFix extends PipeConnectionFix {

    public ActivablePipeConnectionFix(Schema outputSchema, boolean changesType, String type) {
        super(outputSchema, changesType, type);
    }

    public Dynamic<?> fixTag(Dynamic<?> tag) {
        tag = super.fixTag(tag);
        return tag.set("active", tag.get("isActive").result().get());
    }
}
