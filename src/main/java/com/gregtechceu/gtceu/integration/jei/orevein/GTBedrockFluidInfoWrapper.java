package com.gregtechceu.gtceu.integration.jei.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import net.minecraft.core.Holder;

public class GTBedrockFluidInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final Holder<BedrockFluidDefinition> fluid;

    public GTBedrockFluidInfoWrapper(Holder<BedrockFluidDefinition> fluid) {
        super(new GTOreVeinWidget(fluid, null));
        this.fluid = fluid;
    }
}
