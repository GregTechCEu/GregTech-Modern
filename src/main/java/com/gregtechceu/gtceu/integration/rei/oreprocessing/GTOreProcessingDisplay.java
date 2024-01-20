package com.gregtechceu.gtceu.integration.rei.oreprocessing;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.integration.GTOreByProductWidget;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;

public class GTOreProcessingDisplay extends ModularDisplay<GTOreByProductWidget> {
    public GTOreProcessingDisplay(Material material) {
        super(() -> new GTOreByProductWidget(material), GTOreProcessingDisplayCategory.CATEGORY);
    }
}
