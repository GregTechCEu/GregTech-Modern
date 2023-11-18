package com.gregtechceu.gtceu.integration.emi.orevein;

import com.gregtechceu.gtceu.api.data.worldgen.GTOreDefinition;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.integration.GTOreVeinWidget;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GTEmiOreVein extends ModularEmiRecipe<WidgetGroup> {
    private final GTOreDefinition oreDefinition;

    public GTEmiOreVein(GTOreDefinition oreDefinition){
        super(() -> new GTOreVeinWidget(oreDefinition));
        this.oreDefinition = oreDefinition;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return GTOreVeinEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return GTRegistries.ORE_VEINS.getKey(oreDefinition);
    }
}
