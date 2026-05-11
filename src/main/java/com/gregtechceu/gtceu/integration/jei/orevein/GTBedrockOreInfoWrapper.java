package com.gregtechceu.gtceu.integration.jei.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.gregtechceu.gtceu.integration.xei.widgets.GTOreVeinWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import net.minecraft.core.Holder;

public class GTBedrockOreInfoWrapper extends ModularWrapper<GTOreVeinWidget> {

    public final Holder<BedrockOreDefinition> bedrockOre;

    public GTBedrockOreInfoWrapper(Holder<BedrockOreDefinition> bedrockOre) {
        super(new GTOreVeinWidget(bedrockOre, null));
        this.bedrockOre = bedrockOre;
    }
}
