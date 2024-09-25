package com.gregtechceu.gtceu.integration.jei.tool;

import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.integration.GTToolWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class GTToolInfoWrapper extends ModularWrapper<Widget> {

    public final Material material;

    public GTToolInfoWrapper(Material mat) {
        super(new GTToolWidget(mat));
        this.material = mat;
    }

}
