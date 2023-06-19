package com.gregtechceu.gtceu.integration.jei;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.common.data.GTMachines;
import com.gregtechceu.gtceu.integration.jei.multipage.MultiblockInfoCategory;
import com.gregtechceu.gtceu.integration.jei.recipe.GTRecipeTypeCategory;
import com.lowdragmc.lowdraglib.LDLib;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIGuiContainer;
import com.lowdragmc.lowdraglib.utils.Position;
import com.lowdragmc.lowdraglib.utils.Size;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.gui.handlers.IGuiClickableArea;
import mezz.jei.api.gui.handlers.IGuiContainerHandler;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.registration.*;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author KilaBash
 * @date 2023/2/25
 * @implNote JEIPlugin
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class JEIPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return GTCEu.id("jei_plugin");
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registry) {
        if (LDLib.isReiLoaded()) return;
        GTCEu.LOGGER.info("JEI register categories");
        IJeiHelpers jeiHelpers = registry.getJeiHelpers();
        registry.addRecipeCategories(new MultiblockInfoCategory(jeiHelpers));
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE) {
            if (recipeType instanceof GTRecipeType gtRecipeType) {
                registry.addRecipeCategories(new GTRecipeTypeCategory(jeiHelpers, gtRecipeType));
            }
        }
    }

    @Override
    public void registerRecipeCatalysts(@Nonnull IRecipeCatalystRegistration registration) {
        if (LDLib.isReiLoaded()) return;
        MultiblockInfoCategory.registerRecipeCatalysts(registration);
        GTRecipeTypeCategory.registerRecipeCatalysts(registration);
        for (MachineDefinition definition : GTMachines.ELECTRIC_FURNACE) {
            registration.addRecipeCatalyst(definition.asStack(), RecipeTypes.SMELTING);
        }
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        if (LDLib.isReiLoaded()) return;
        GTCEu.LOGGER.info("JEI register");
        MultiblockInfoCategory.registerRecipes(registration);
        GTRecipeTypeCategory.registerRecipes(registration);
    }

    @Override
    public void registerIngredients(@Nonnull IModIngredientRegistration registry) {
        if (LDLib.isReiLoaded()) return;
        GTCEu.LOGGER.info("JEI register ingredients");
    }

    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registry) {
        if (LDLib.isReiLoaded()) return;
        GTCEu.LOGGER.info("JEI register click areas");
        Pattern progressWidgetId = Pattern.compile("progress");
        for (GTRecipeType type : GTRegistries.RECIPE_TYPES.values()) {
            var group = type.createDefaultUITemplate(false);
            var widget = group.getFirstWidgetById(progressWidgetId);
            if (widget != null) {
                Size widgetSize = widget.getSize();
                Position widgetPos = widget.getPosition();
                registry.addGuiContainerHandler(ModularUIGuiContainer.class, new IGuiContainerHandler<>() {
                    @Override
                    public Collection<IGuiClickableArea> getGuiClickableAreas(ModularUIGuiContainer containerScreen, double mouseX, double mouseY) {
                        if (containerScreen.modularUI.holder instanceof IUIMachine machine && machine.self() instanceof IRecipeLogicMachine recipeLogicMachine && recipeLogicMachine.getRecipeType() == type) {
                            IGuiClickableArea clickableArea = IGuiClickableArea.createBasic(widgetPos.x, widgetPos.y + widgetSize.height, widgetSize.width, widgetSize.height, GTRecipeTypeCategory.TYPES.apply(type));
                            return List.of(clickableArea);
                        }
                        return Collections.emptyList();
                    }
                });
            }
        }
    }
}
