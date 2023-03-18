package com.gregtechceu.gtceu.api.data.chemical.fluid;

public class FluidTypes {

    public static final FluidType LIQUID = new FluidTypeLiquid("liquid", null, null, "gregtech.fluid.generic");

    public static final FluidType ACID = new FluidTypeAcid("acid", null, null, "gregtech.fluid.generic");

    public static final FluidType GAS = new FluidTypeGas("gas", null, null, "gregtech.fluid.generic");

    public static final FluidType PLASMA = new FluidTypePlasma("plasma", "plasma", null, "gregtech.fluid.plasma");
}
