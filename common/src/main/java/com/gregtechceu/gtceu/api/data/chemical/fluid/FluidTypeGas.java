package com.gregtechceu.gtceu.api.data.chemical.fluid;


import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidTypeGas extends FluidType {

    public FluidTypeGas(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
        density = -100;
        viscosity = 200;
    }

    @Override
    public void addAdditionalTooltips(List<Component> tooltips) {
        tooltips.add(Component.translatable("gregtech.fluid.state_gas"));
    }
}
