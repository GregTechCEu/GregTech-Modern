package com.gregtechceu.gtceu.integration.rei.oreprocessing;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.integration.GTOreProcessingWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;

public class GTOreProcessingDisplay extends ModularDisplay<WidgetGroup> {
    public GTOreProcessingDisplay(Material material) {
        super(() -> new GTOreProcessingWidget(material), GTOreProcessingDisplayCategory.CATEGORY);
    }
}
