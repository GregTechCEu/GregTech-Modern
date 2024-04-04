package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class GTEmiRecipe extends ModularEmiRecipe<WidgetGroup> {
    final GTRecipeTypeEmiCategory category;
    final GTRecipe recipe;
    private final GTRecipeWidget gtRecipeWidget;
    private static final ResourceLocation BUTTONS = GTCEu.id("textures/gui/widget/button_oc.png");

    public GTEmiRecipe(GTRecipeTypeEmiCategory category, GTRecipe recipe) {
        super(() -> new GTRecipeWidget(recipe));
        this.gtRecipeWidget = (GTRecipeWidget) super.widget.get();
        this.category = category;
        this.recipe = recipe;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        super.addWidgets(widgets);
        widgets.addButton(gtRecipeWidget.getSize().width + 5, 3, 12, 12, 0, 0, BUTTONS,
            () -> true, (mouseX, mouseY, button) -> gtRecipeWidget.setRecipeOC(button));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }
}
