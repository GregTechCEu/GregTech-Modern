package com.gregtechceu.gtceu.integration.emi.multipage;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

import net.minecraft.resources.ResourceLocation;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiblockInfoEmiRecipe extends ModularEmiRecipe<WidgetGroup> {

    private final MultiblockMachineDefinition definition;
    private SlotWidget slotWidget;

    public MultiblockInfoEmiRecipe(MultiblockMachineDefinition definition) {
        super(() -> PatternPreviewWidget.getPatternWidget(definition));
        this.definition = definition;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);
        // numbers gotten from the size of the widget
        slotWidget = new SlotWidget(EmiStack.of(definition.getItem().asItem()), 138, 12)
                .recipeContext(this)
                .drawBack(false);

        widgets.add(slotWidget);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return MultiblockInfoEmiCategory.CATEGORY;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return definition.getId();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of(EmiStack.of(definition.getItem()));
    }
}
