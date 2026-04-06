package com.gregtechceu.gtceu.api.datafixer.fixes;

import net.minecraft.util.datafix.fixes.ItemStackComponentRemainderFix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import org.jetbrains.annotations.NotNull;

public abstract class ToolBehaviorRemainderFix extends ItemStackComponentRemainderFix {

    private final String behaviorId;
    private final String newBehaviorId;

    public ToolBehaviorRemainderFix(Schema outputSchema, String name, String behaviorId) {
        this(outputSchema, name, behaviorId, behaviorId);
    }

    public ToolBehaviorRemainderFix(Schema outputSchema, String name, String behaviorId, String newBehaviorId) {
        super(outputSchema, name, "gtceu:tool_behaviors");
        this.behaviorId = behaviorId;
        this.newBehaviorId = newBehaviorId;
    }

    @Override
    protected final <T> @NotNull Dynamic<T> fixComponent(@NotNull Dynamic<T> tag) {
        return tag.renameAndFixField(behaviorId, newBehaviorId, this::fixBehavior);
    }

    protected abstract <T> @NotNull Dynamic<T> fixBehavior(@NotNull Dynamic<T> tag);
}
