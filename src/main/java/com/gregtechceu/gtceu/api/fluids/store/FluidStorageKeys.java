package com.gregtechceu.gtceu.api.fluids.store;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidState;

import java.util.function.UnaryOperator;

public final class FluidStorageKeys {

    public static final FluidStorageKey LIQUID = new FluidStorageKey(GTCEu.id("liquid"),
            MaterialIconType.liquid,
            UnaryOperator.identity(),
            m -> m.hasProperty(PropertyKey.DUST) ? "gtceu.fluid.liquid_generic" : "gtceu.fluid.generic",
            FluidState.LIQUID, 0);

    public static final FluidStorageKey GAS = new FluidStorageKey(GTCEu.id("gas"),
            MaterialIconType.gas,
            UnaryOperator.identity(),
            m -> {
                if (m.hasProperty(PropertyKey.DUST)) {
                    return "gtceu.fluid.gas_vapor";
                }
                if (m.isElement()) {
                    return "gtceu.fluid.gas_generic";
                }
                return "gtceu.fluid.generic";
            },
            FluidState.GAS, 0);

    public static final FluidStorageKey PLASMA = new FluidStorageKey(GTCEu.id("plasma"),
            MaterialIconType.plasma,
            s -> s + "_plasma", m -> "gtceu.fluid.plasma",
            FluidState.PLASMA, -1);

    public static final FluidStorageKey MOLTEN = new FluidStorageKey(GTCEu.id("molten"),
            MaterialIconType.molten,
            s -> "molten_" + s, m -> "gtceu.fluid.molten",
            FluidState.LIQUID, -1);

    private FluidStorageKeys() {}
}
