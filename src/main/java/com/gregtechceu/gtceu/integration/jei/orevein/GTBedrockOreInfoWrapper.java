package com.gregtechceu.gtceu.integration.jei.orevein;

import com.gregtechceu.gtceu.api.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTBedrockOreInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final BedrockOreDefinition bedrockOre;

    public GTBedrockOreInfoWrapper(BedrockOreDefinition bedrockOre) {
        super(new GTOreVeinWidget(bedrockOre));
        this.bedrockOre = bedrockOre;
    }
}
