package com.gregtechceu.gtceu.api.data.chemical.fluid;

import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class FluidTypeAcid extends FluidTypeLiquid {

    public FluidTypeAcid(@Nonnull String name, @Nullable String prefix, @Nullable String suffix, @Nonnull String localization) {
        super(name, prefix, suffix, localization);
    }

    @Override
    public void addAdditionalTooltips(List<Component> tooltips) {
        tooltips.add(Component.translatable("gregtech.fluid.state_liquid"));
        tooltips.add(Component.translatable("gregtech.fluid.type_acid.tooltip"));
    }
}
