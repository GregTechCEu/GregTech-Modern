package com.gregtechceu.gtceu.api.fluids.store;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.api.data.chemical.material.info.MaterialIconType;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.gregtechceu.gtceu.api.data.chemical.material.properties.PropertyKey;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import org.jetbrains.annotations.NotNull;

public final class FluidStorageKeys {

    public static final FluidStorageKey LIQUID = new FluidStorageKey(GTCEu.id("liquid"),
            MaterialIconType.liquid,
            m -> prefixedRegisteredName("liquid_", FluidStorageKeys.LIQUID, m),
            m -> m.hasProperty(PropertyKey.DUST) ? "gtceu.fluid.liquid_generic" : "gtceu.fluid.generic",
            FluidState.LIQUID, 0);

    public static final FluidStorageKey GAS = new FluidStorageKey(GTCEu.id("gas"),
            MaterialIconType.gas,
            m -> postfixedRegisteredName("_gas", FluidStorageKeys.GAS, m),
            m -> {
                if (m.hasProperty(PropertyKey.DUST)) {
                    return "gtceu.fluid.gas_vapor";
                }
                if (m.isElement()) {
                    FluidProperty property = m.getProperty(PropertyKey.FLUID);
                    if (m.isElement() || (property != null && property.getPrimaryKey() != FluidStorageKeys.LIQUID)) {
                        return "gtceu.fluid.gas_generic";
                    }
                }
                return "gtceu.fluid.generic";
            },
            FluidState.GAS, 0);

    public static final FluidStorageKey PLASMA = new FluidStorageKey(GTCEu.id("plasma"),
            MaterialIconType.plasma,
            m -> m.getName() + "_plasma",
            m -> "gtceu.fluid.plasma",
            FluidState.PLASMA, -1);

    public static final FluidStorageKey MOLTEN = new FluidStorageKey(GTCEu.id("molten"), CustomTags.MOLTEN_FLUIDS,
            MaterialIconType.molten,
            m -> "molten_" + m.getName(),
            m -> "gtceu.fluid.molten",
            FluidState.LIQUID, -1);

    private FluidStorageKeys() {}

    private static @NotNull String prefixedRegisteredName(@NotNull String prefix, @NotNull FluidStorageKey key,
                                                          @NotNull Material material) {
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        if (property != null && property.getPrimaryKey() != key) {
            return prefix + material.getName();
        }
        return material.getName();
    }

    private static @NotNull String postfixedRegisteredName(@NotNull String postfix, @NotNull FluidStorageKey key,
                                                           @NotNull Material material) {
        FluidProperty property = material.getProperty(PropertyKey.FLUID);
        if (property != null && property.getPrimaryKey() != key) {
            return material.getName() + postfix;
        }
        return material.getName();
    }
}
