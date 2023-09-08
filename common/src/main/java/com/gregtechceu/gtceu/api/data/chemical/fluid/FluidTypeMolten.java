package com.gregtechceu.gtceu.api.data.chemical.fluid;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidTypeMolten extends FluidTypeLiquid {

    public FluidTypeMolten(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }
}