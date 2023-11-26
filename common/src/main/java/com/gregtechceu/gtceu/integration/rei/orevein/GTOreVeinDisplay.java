package com.gregtechceu.gtceu.integration.rei.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class GTOreVeinDisplay extends ModularDisplay<WidgetGroup> {
    private final GTOreDefinition oreDefinition;

    public GTOreVeinDisplay(GTOreDefinition oreDefinition) {
        super(() -> new GTOreVeinWidget(oreDefinition), GTOreVeinDisplayCategory.CATEGORY);
        this.oreDefinition = oreDefinition;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        List<EntryIngredient> ingredients = new ArrayList<>();
        for (ItemStack outPut:GTOreVeinWidget.getContainedOresAndBlocks(oreDefinition)) {
            ingredients.add(EntryIngredients.of(outPut));
        }
        return ingredients;
    }
}
