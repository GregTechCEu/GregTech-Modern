package com.gregtechceu.gtceu.api.datafixer.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.ItemStackTagFix;

public abstract class ToolBehaviorFix extends ItemStackTagFix {

    private final String behaviorId;
    private final String newBehaviorId;

    public ToolBehaviorFix(Schema outputSchema, String name, String behaviorId) {
        this(outputSchema, name, behaviorId, behaviorId);
    }

    public ToolBehaviorFix(Schema outputSchema, String name, String behaviorId, String newBehaviorId) {
        super(outputSchema, name, itemId -> true);
        this.behaviorId = behaviorId;
        this.newBehaviorId = newBehaviorId;
    }

    @Override
    protected final <T> Dynamic<T> fixItemStackTag(Dynamic<T> tag) {
        Dynamic<T> behavior = tag.remove(behaviorId);
        behavior = fixBehavior(behavior);
        return tag.set(newBehaviorId, behavior);
    }

    protected abstract <T> Dynamic<T> fixBehavior(Dynamic<T> tag);
}
