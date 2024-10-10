package com.gregtechceu.gtceu.api.graphnet;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractGroupData {

    protected NetGroup group;

    public void withGroup(NetGroup group) {
        this.group = group;
    }

    public abstract boolean mergeAllowed(@NotNull AbstractGroupData other);

    @Nullable
    public AbstractGroupData merge(@NotNull AbstractGroupData other) {
        return null;
    }
}
