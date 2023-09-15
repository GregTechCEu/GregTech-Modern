package com.gregtechceu.gtceu.api.data.chemical.fluid;

public class FluidTypes {

    public static final FluidType LIQUID = new FluidTypeLiquid("liquid", null, null, "gtceu.fluid.generic");

    public static final FluidType ACID = new FluidTypeAcid("acid", null, null, "gtceu.fluid.generic");

    public static final FluidType GAS = new FluidTypeGas("gas", null, null, "gtceu.fluid.generic");

    public static final FluidType PLASMA = new FluidTypePlasma("plasma", "plasma", null, "gtceu.fluid.plasma");

    public static final FluidType MOLTEN = new FluidTypeMolten("molten", "molten", null, "gtceu.fluid.molten");

}
