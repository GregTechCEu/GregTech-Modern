package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.common.data.GTRecipeTypes;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.EmiCraftContext;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GTEmiRecipeHandler implements StandardRecipeHandler<ModularUIContainer> {

    @Override
    public List<Slot> getInputSources(ModularUIContainer handler) {
        return handler.getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT || e.isPlayerContainer || e.isPlayerHotBar)
                .map(SlotWidget::getHandler)
                .toList();
    }

    @Override
    public List<Slot> getCraftingSlots(ModularUIContainer handler) {
        return handler.getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT)
                .map(SlotWidget::getHandler)
                .toList();
    }

    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return (recipe instanceof GTEmiRecipe) || (recipe.getCategory() == VanillaEmiRecipeCategories.SMELTING);
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        return machineSupportsRecipe(context.getScreenHandler(), recipe) &&
                StandardRecipeHandler.super.canCraft(recipe, context);
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        if (!machineSupportsRecipe(context.getScreenHandler(), recipe)) {
            return List.of(ClientTooltipComponent.create(
                    Component.translatable("emi.inapplicable").getVisualOrderText()));
        }
        return StandardRecipeHandler.super.getTooltip(recipe, context);
    }

    private static @Nullable GTRecipeType getGTRecipeType(EmiRecipe recipe) {
        if (recipe instanceof GTEmiRecipe gtRecipe) {
            return gtRecipe.recipe.recipeType;
        }
        // Needed to fill vanilla smelting recipes in GT furnaces.
        if (recipe.getCategory() == VanillaEmiRecipeCategories.SMELTING) {
            return GTRecipeTypes.FURNACE_RECIPES;
        }
        return null;
    }

    private boolean machineSupportsRecipe(ModularUIContainer container, EmiRecipe recipe) {
        GTRecipeType type = getGTRecipeType(recipe);
        if (type == null)
            return false;

        IUIHolder holder = container.getModularUI().holder;
        if (holder instanceof IRecipeLogicMachine machine) {
            return machine.getRecipeType() == type;
        }
        // For multiblock parts, check if a linked controller supports this recipe type.
        // This lets us filter relevant recipes in input buses.
        if (holder instanceof IMultiPart part) {
            // Skip screens without crafting inputs (output buses, maintenance hatches...)
            if (getCraftingSlots(container).isEmpty())
                return false;
            for (IMultiController controller : part.getControllers()) {
                if (controller instanceof IRecipeLogicMachine machine &&
                        machine.getRecipeType() == type) {
                    return true;
                }
            }
        }
        return false;
    }
}
