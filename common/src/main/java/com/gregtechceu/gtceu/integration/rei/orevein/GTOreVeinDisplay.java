package com.gregtechceu.gtceu.integration.rei.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;
import me.shedaniel.rei.api.common.entry.EntryIngredient;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;

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
        List<EntryIngredient> outputs = new ArrayList<>();
        List<BlockState> blocks = oreDefinition.getVeinGenerator().getAllBlocks();
        outputs.add(EntryIngredients.of((ItemLike) blocks));
        return outputs;
    }
}
