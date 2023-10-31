package com.gregtechceu.gtceu.integration.jei.oreprocessing;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.integration.GTOreProcessingWidget;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTOreProcessingInfoWrapper extends ModularWrapper<GTOreProcessingWidget> {
    public final Material material;

    public GTOreProcessingInfoWrapper(Material mat) {
        super(new GTOreProcessingWidget(mat));
        this.material = mat;
    }

}
