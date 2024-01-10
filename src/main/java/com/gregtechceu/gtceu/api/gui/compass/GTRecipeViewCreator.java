package com.gregtechceu.gtceu.api.gui.compass;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.integration.GTRecipeWidget;
import com.lowdragmc.lowdraglib.gui.compass.component.RecipeComponent;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

/**
 * @author KilaBash
 * @date 2023/7/30
 * @implNote GTRecipeViewCreator
 */
public class GTRecipeViewCreator implements RecipeComponent.RecipeViewCreator {
    @Override
    public ItemStack getWorkstation(Recipe<?> recipe) {
        if (recipe instanceof GTRecipe gtRecipe) {
            if (gtRecipe.recipeType.getIconSupplier() != null) {
                return gtRecipe.recipeType.getIconSupplier().get();
            }
        }
        return new ItemStack(Items.BARRIER);
    }

    @Override
    public WidgetGroup getViewWidget(Recipe<?> recipe) {
        if (recipe instanceof GTRecipe gtRecipe) {
            var widget = new GTRecipeWidget(gtRecipe);
            widget.addSelfPosition(4, 4);
            var recipeGroup = new WidgetGroup(0, 0, widget.getSize().width + 8, widget.getSize().height + 8);
            recipeGroup.setBackground(GuiTextures.BACKGROUND);
            recipeGroup.addWidget(widget);
            return recipeGroup;

        }
        return new WidgetGroup();
    }

    @Override
    public boolean test(Recipe<?> recipe) {
        return recipe instanceof GTRecipe;
    }
}
