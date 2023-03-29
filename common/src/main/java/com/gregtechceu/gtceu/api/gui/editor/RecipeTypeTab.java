package com.gregtechceu.gtceu.api.gui.editor;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.gui.editor.annotation.RegisterUI;
import com.lowdragmc.lowdraglib.gui.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.ProgressWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.msic.FluidStorage;
import com.lowdragmc.lowdraglib.msic.ItemStackTransfer;
import com.lowdragmc.lowdraglib.side.fluid.IFluidStorage;
import net.minecraft.world.item.Items;

import java.util.Arrays;

/**
 * @author KilaBash
 * @date 2023/3/29
 * @implNote RecipeTypeMenu
 */
@RegisterUI(name = "recipe_type_tab", group = "menu")
public class RecipeTypeTab extends MenuTab {

    protected TreeBuilder.Menu createMenu() {
        var recipeTypeMenu = TreeBuilder.Menu.start();
        var currentProject = editor.getCurrentProject();
        if (currentProject instanceof RecipeTypeUIProject project) {
            for (GTRecipeType recipeType : GTRegistries.RECIPE_TYPES) {
                IGuiTexture icon;
                if (recipeType.getIconSupplier() != null) {
                    icon = new ItemStackTexture(recipeType.getIconSupplier().get());
                } else {
                    icon = new ItemStackTexture(Items.BARRIER);
                }
                recipeTypeMenu.leaf(icon, recipeType.registryName.toLanguageKey(), () -> {
                    IFluidStorage[] importFluids = new IFluidStorage[recipeType.getMaxInputs(FluidRecipeCapability.CAP)];
                    IFluidStorage[] exportFluids = new IFluidStorage[recipeType.getMaxOutputs(FluidRecipeCapability.CAP)];
                    Arrays.fill(importFluids, new FluidStorage(1000));
                    Arrays.fill(exportFluids, new FluidStorage(1000));
                    var widget = recipeType.createUITemplate(ProgressWidget.JEIProgress,
                            new ItemStackTransfer(recipeType.getMaxInputs(ItemRecipeCapability.CAP)),
                            new ItemStackTransfer(recipeType.getMaxOutputs(ItemRecipeCapability.CAP)),
                            importFluids, exportFluids, false, false);
                    for (Widget children : widget.widgets) {
                        project.root.addWidget(children);
                    }
                });
            }
        } else {
            recipeTypeMenu.leaf("only available in recipe type project", () -> {});
        }
        return recipeTypeMenu;
    }

}
