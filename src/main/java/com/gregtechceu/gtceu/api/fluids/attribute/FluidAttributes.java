package com.gregtechceu.gtceu.api.fluids.attribute;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public final class FluidAttributes {

    /**
     * Attribute for acidic fluids.
     */
    public static final FluidAttribute ACID = new FluidAttribute(GTCEu.id("acid"),
            list -> list.accept(
                    Component.translatable("material.gtceu.fluid_property.acid").withStyle(ChatFormatting.GOLD)),
            list -> list.accept(
                    Component.translatable("tooltip.gtceu.fluid_pipe.acid_proof").withStyle(ChatFormatting.GOLD)));

    private FluidAttributes() {}
}
