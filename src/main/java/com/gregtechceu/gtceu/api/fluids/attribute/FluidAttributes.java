package com.gregtechceu.gtceu.api.fluids.attribute;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.network.chat.Component;

public final class FluidAttributes {

    /**
     * Attribute for acidic fluids.
     */
    public static final FluidAttribute ACID = new FluidAttribute(GTCEu.id("acid"),
            list -> list.accept(Component.translatable("fluid.gtceu.tooltip.acid")),
            list -> list.accept(Component.translatable("gtceu.fluid_container.acid_proof")));

    private FluidAttributes() {}
}
