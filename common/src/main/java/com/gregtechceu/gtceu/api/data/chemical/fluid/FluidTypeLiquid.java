package com.gregtechceu.gtceu.api.data.chemical.fluid;


import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidTypeLiquid extends FluidType {

    public FluidTypeLiquid(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
        viscosity = 1000;
    }

    @Override
    public void addAdditionalTooltips(List<Component> tooltips) {
        tooltips.add(Component.translatable("gtceu.fluid.state_liquid"));
    }
}
