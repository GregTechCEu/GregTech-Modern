package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.util.StringRepresentable;

import java.util.function.Supplier;

public record NetLogicEntryType<T extends NetLogicEntry<T, ?>>(String id, Supplier<T> supplier)
        implements StringRepresentable {

    public NetLogicEntryType {
        NetLogicRegistry.register(this);
    }

    public T getNew() {
        return supplier.get();
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
