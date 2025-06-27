package com.gregtechceu.gtceu.api.fluids.attribute;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;

public final class FluidAttributes {

    /**
     * Attribute for acidic fluids.
     */
    public static final FluidAttribute ACID = new FluidAttribute(GTCEu.id("acid"),
            list -> list.accept(Component.translatable("tooltip.gtceu.fluid_property.acid")),
            list -> list.accept(Component.translatable("tooltip.gtceu.fluid_pipe.acid_proof")));

    private FluidAttributes() {}
}
