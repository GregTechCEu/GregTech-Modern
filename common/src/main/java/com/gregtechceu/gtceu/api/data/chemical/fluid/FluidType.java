package com.gregtechceu.gtceu.api.data.chemical.fluid;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import lombok.Getter;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class FluidType {

    private static final Map<String, FluidType> FLUID_TYPES = new HashMap<>();

    private final String name;
    private final String prefix;
    private final String suffix;
    protected final String localization;
    @Getter
    protected int density = 1000;
    @Getter
    protected int luminance = 0;
    @Getter
    protected int viscosity = 1000;

    public FluidType(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        if (FLUID_TYPES.get(name) != null)
            throw new IllegalArgumentException("Cannot register FluidType with duplicate name: " + name);

        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
        this.localization = localization;
        FLUID_TYPES.put(name, this);
    }

    public String getNameForMaterial(@Nonnull Material material) {
        StringBuilder builder = new StringBuilder();

        if (this.prefix != null)
            builder.append(this.prefix).append(".");

        builder.append(material);

        if (this.suffix != null)
            builder.append(".").append(this.suffix);

        return builder.toString();
    }

    public String getLocalization() {
        return this.localization;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getName() {
        return this.name;
    }

    public void addAdditionalTooltips(List<Component> tooltips) {
    }

    @Nullable
    public static FluidType getByName(@Nonnull String name) {
        return FLUID_TYPES.get(name);
    }
}
