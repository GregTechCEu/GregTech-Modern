package com.gregtechceu.gtceu.integration.jei.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import net.minecraft.core.Holder;

public class GTOreVeinInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final Holder<GTOreDefinition> oreDefinition;

    public GTOreVeinInfoWrapper(Holder<GTOreDefinition> oreDefinition) {
        super(new GTOreVeinWidget(oreDefinition));
        this.oreDefinition = oreDefinition;
    }
}
