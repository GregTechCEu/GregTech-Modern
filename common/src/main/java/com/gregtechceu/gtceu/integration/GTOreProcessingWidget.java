package com.gregtechceu.gtceu.integration;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.IGui2Renderer;
import me.shedaniel.rei.api.client.gui.Renderer;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

/**
 * @author Rundas
 * @implNote GTOreProcessingWidget
 */
public class GTOreProcessingWidget extends WidgetGroup {
    public GTOreProcessingWidget(Material material) {
        super(0, 0, 186, 166);
        setClientSideWidget();
        Renderer rawOreTexture = IGui2Renderer.toDrawable(new ItemStackTexture(ChemicalHelper.get(rawOre, material)));
    }
}
