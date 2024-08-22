package com.gregtechceu.gtceu.api.capability.data.query;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public final class DataQueryFormat implements StringRepresentable {

    public static final DataQueryFormat RECIPE = create("gtceu.data_format.query.recipe");
    public static final DataQueryFormat COMPUTATION = create("gtceu.data_format.query.computation");

    public static DataQueryFormat create(@NotNull String name) {
        return new DataQueryFormat(name);
    }

    private final @NotNull String name;

    private DataQueryFormat(@NotNull String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }
}
