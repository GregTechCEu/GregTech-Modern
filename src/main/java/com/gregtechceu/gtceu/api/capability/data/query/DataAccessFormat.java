package com.gregtechceu.gtceu.api.capability.data.query;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public final class DataAccessFormat implements StringRepresentable {

    public static final DataAccessFormat STANDARD = create("gtceu.data_format.access.standard",
            DataQueryFormat.RECIPE);
    public static final DataAccessFormat COMPUTATION = create("gtceu.data_format.access.computation",
            DataQueryFormat.COMPUTATION);

    public static final DataAccessFormat UNIVERSAL = new DataAccessFormat("gtceu.data_format.access.universal",
            null);

    private final Set<DataQueryFormat> supportedFormats;

    public static DataAccessFormat create(@NotNull String name, DataQueryFormat... allowedFormats) {
        return new DataAccessFormat(name, new ObjectOpenHashSet<>(allowedFormats));
    }

    private final @NotNull String name;

    private DataAccessFormat(@NotNull String name, Set<DataQueryFormat> supportedFormats) {
        this.name = name;
        this.supportedFormats = supportedFormats;
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    @Contract("_ -> this")
    public DataAccessFormat support(DataQueryFormat format) {
        if (supportedFormats != null) this.supportedFormats.add(format);
        return this;
    }

    @Contract("_ -> this")
    public DataAccessFormat notSupport(DataQueryFormat format) {
        if (supportedFormats != null) this.supportedFormats.remove(format);
        return this;
    }

    public boolean supportsFormat(DataQueryFormat format) {
        if (supportedFormats == null) return true;
        else return supportedFormats.contains(format);
    }
}
