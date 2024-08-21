package com.gregtechceu.gtceu.api.graphnet.pipenet.transfer;

import net.minecraft.util.IStringSerializable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class TransferControl<T> implements IStringSerializable {

    private final String name;

    public TransferControl(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

    public abstract @NotNull T get(@Nullable Object potentialHolder);

    public abstract @NotNull T getNoPassage();

    public T cast(Object o) {
        return (T) o;
    }
}
