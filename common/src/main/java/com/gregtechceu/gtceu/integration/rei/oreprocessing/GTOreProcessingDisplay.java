package com.gregtechceu.gtceu.integration.rei.oreprocessing;

import com.gregtechceu.gtceu.api.data.chemical.ChemicalHelper;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.integration.GTOreProcessingWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;

import java.util.ArrayList;
import java.util.List;

import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.*;

public class GTOreProcessingDisplay extends ModularDisplay<WidgetGroup> {

    private final Material material;

    public GTOreProcessingDisplay(Material material) {
        super(() -> new GTOreProcessingWidget(material), GTOreProcessingDisplayCategory.CATEGORY);
        this.material = material;
    }

    @Override
    public List<EntryIngredient> getInputEntries() {
        List<EntryIngredient> ingredients = new ArrayList<>();
        ingredients.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(ore, material)));
        ingredients.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(rawOre, material)));
        return ingredients;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> outputs = new ArrayList<>();
        outputs.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(crushed, material)));
        outputs.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(crushedPurified, material)));
        outputs.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(crushedRefined, material)));
        outputs.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(dust, material)));
        outputs.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(dustImpure, material)));
        outputs.add(EntryIngredients.ofItemTag(ChemicalHelper.getTag(dustPure, material)));
        return outputs;
    }
}
