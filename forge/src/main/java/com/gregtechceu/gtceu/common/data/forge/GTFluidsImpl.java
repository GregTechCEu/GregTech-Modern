package com.gregtechceu.gtceu.common.data.forge;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.FluidProperty;
import com.tterrag.registrate.util.entry.FluidEntry;

public class GTFluidsImpl {
    public static void setPropertyFluid(FluidProperty prop, FluidEntry<?> entry) {
        prop.setFluid(entry::getSource);
    }
}
