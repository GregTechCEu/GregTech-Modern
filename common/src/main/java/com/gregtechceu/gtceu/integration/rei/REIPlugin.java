package com.gregtechceu.gtceu.integration.rei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.item.tool.GTToolType;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeTypeCategory;
import com.gregtechceu.gtceu.integration.rei.multipage.MultiblockInfoDisplayCategory;
import com.gregtechceu.gtceu.integration.rei.recipe.GTRecipeTypeDisplayCategory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.client.registry.screen.ScreenRegistry;
import me.shedaniel.rei.api.client.registry.screen.SimpleClickArea;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static me.shedaniel.rei.plugin.common.BuiltinPlugin.SMELTING;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote REIPlugin
 */
public class REIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new MultiblockInfoDisplayCategory());
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                registry.add(new GTRecipeTypeDisplayCategory(gtRecipeType));
            }
        }
        // workstations
        MultiblockInfoDisplayCategory.registerWorkStations(registry);
        GTRecipeTypeDisplayCategory.registerWorkStations(registry);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            registry.addWorkstations(SMELTING, EntryStacks.of(definition.asStack()));
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        GTRecipeTypeDisplayCategory.registerDisplays(registry);
        MultiblockInfoDisplayCategory.registerDisplays(registry);
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        for (GTToolType toolType : GTToolType.values()) {
            registry.group(GTCEu.id("tool/" + toolType.name()), Component.translatable(toolType.getUnlocalizedName()), EntryIngredients.ofItemTag(toolType.itemTag));
        }
    }

    @Override
    public void registerScreens(ScreenRegistry registry) {
        Pattern progressWidgetId = java.util.regex.Pattern.compile("progress");
        for (GTRecipeType type : GTRegistries.RECIPE_TYPES.values()) {
            var group = type.createDefaultUITemplate(true);
            var widget = group.getFirstWidgetById(progressWidgetId);
            if (widget != null) {
                Size widgetSize = widget.getSize();
                Position widgetPos = widget.getPosition();
                registry.registerContainerClickArea(screen -> {
                    if (screen.modularUI.holder instanceof IUIMachine machine && machine.self() instanceof IRecipeLogicMachine recipeLogicMachine && recipeLogicMachine.getRecipeType() == type) {
                        return new Rectangle(widgetPos.x, widgetPos.y + widgetSize.height, widgetSize.width, widgetSize.height);
                    }
                    return new Rectangle();
                }, ModularUIGuiContainer.class, GTRecipeTypeDisplayCategory.CATEGORIES.apply(type));
            }
        }
    }
}
