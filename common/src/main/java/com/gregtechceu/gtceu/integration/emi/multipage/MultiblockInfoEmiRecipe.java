package com.gregtechceu.gtceu.integration.emi.multipage;


import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class MultiblockInfoEmiRecipe extends ModularEmiRecipe<WidgetGroup> {
    public final MultiblockMachineDefinition definition;

    public MultiblockInfoEmiRecipe(MultiblockMachineDefinition definition) {
        super(() -> PatternPreviewWidget.getPatternWidget(definition));
        this.definition = definition;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return MultiblockInfoEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return definition.getId();
    }
}
