package com.gregtechceu.gtceu.api.gui.editor;

import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.lowdragmc.lowdraglib.gui.editor.annotation.RegisterUI;
import com.lowdragmc.lowdraglib.gui.editor.ui.menu.MenuTab;
import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
            Map<String, List<GTRecipeType>> categories = new LinkedHashMap<>();
            for (GTRecipeType recipeType : GTRegistries.RECIPE_TYPES) {
                categories.computeIfAbsent(recipeType.group, group -> new ArrayList<>()).add(recipeType);
            }
            categories.forEach((groupName, recipeTypes) -> recipeTypeMenu.branch(groupName, menu -> {
                for (GTRecipeType recipeType : recipeTypes) {
                    IGuiTexture icon;
                    if (recipeType.getIconSupplier() != null) {
                        icon = new ItemStackTexture(recipeType.getIconSupplier().get());
                    } else {
                        icon = new ItemStackTexture(Items.BARRIER);
                    }
                    menu.leaf(icon, recipeType.registryName.toLanguageKey(), () -> {
                        project.root.clearAllWidgets();
                        var widget = recipeType.createDefaultUITemplate(true);
                        project.root.setSize(widget.getSize());
                        for (Widget children : widget.widgets) {
                            project.root.addWidget(children);
                        }
                        project.setRecipeType(recipeType);
                        var resources = editor.getResourcePanel().getResources();
                        if (resources != null && resources.resources.get(RecipeTypeResource.RESOURCE_NAME) instanceof RecipeTypeResource resource) {
                            resource.switchRecipeType(recipeType);
                            editor.getResourcePanel().loadResource(resources, false);
                        }
                    });
                }
            }));
        } else {
            recipeTypeMenu.leaf("only available in recipe type project", () -> {});
        }
        return recipeTypeMenu;
    }

}
