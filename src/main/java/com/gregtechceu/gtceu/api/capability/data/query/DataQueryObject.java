package com.gregtechceu.gtceu.api.capability.data.query;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

public abstract class DataQueryObject {

    private static int ID = 0;

    private final int id;

    @Getter
    @Setter
    private boolean shouldTriggerWalker = false;

    public DataQueryObject() {
        this.id = ID++;
    }

    @NotNull
    public abstract DataQueryFormat getFormat();

    @Override
    public int hashCode() {
        return id;
    }
}
