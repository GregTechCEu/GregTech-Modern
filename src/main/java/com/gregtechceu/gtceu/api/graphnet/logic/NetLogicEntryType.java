package com.gregtechceu.gtceu.api.graphnet.logic;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public record NetLogicEntryType<T extends NetLogicEntry<T, ?>>(String id, Supplier<@NotNull T> supplier)
        implements StringRepresentable {

    public NetLogicEntryType(String id, Supplier<@NotNull T> supplier) {
        this.id = id;
        this.supplier = supplier;
        NetLogicRegistry.register(this);
    }

    @NotNull
    public T getNew() {
        return supplier.get();
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}
