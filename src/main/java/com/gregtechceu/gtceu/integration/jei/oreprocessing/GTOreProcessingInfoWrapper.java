package com.gregtechceu.gtceu.integration.jei.oreprocessing;

import com.gregtechceu.gtceu.api.material.material.Material;
import com.gregtechceu.gtceu.integration.GTOreByProductWidget;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTOreProcessingInfoWrapper extends ModularWrapper<GTOreByProductWidget> {

    public final Material material;

    public GTOreProcessingInfoWrapper(Material mat) {
        super(new GTOreByProductWidget(mat));
        this.material = mat;
    }
}
