package com.gregtechceu.gtceu.integration.jei.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTBedrockFluidInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final BedrockFluidDefinition fluid;

    public GTBedrockFluidInfoWrapper(BedrockFluidDefinition fluid) {
        super(new GTOreVeinWidget(fluid));
        this.fluid = fluid;
    }
}
