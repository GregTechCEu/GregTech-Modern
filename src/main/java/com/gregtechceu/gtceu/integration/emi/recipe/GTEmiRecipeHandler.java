package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.api.machine.feature.IRecipeLogicMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;

import com.lowdragmc.lowdraglib.gui.modular.IUIHolder;
import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.world.inventory.Slot;

import dev.emi.emi.api.recipe.EmiRecipe;
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
        return recipe instanceof GTEmiRecipe;
    }

    @Override
    public boolean canCraft(EmiRecipe recipe, EmiCraftContext<ModularUIContainer> context) {
        return machineSupportsRecipe(context.getScreenHandler(), recipe) &&
                StandardRecipeHandler.super.canCraft(recipe, context);
    }

    private static @Nullable GTRecipeType getGTRecipeType(EmiRecipe recipe) {
        if (recipe instanceof GTEmiRecipe gtRecipe) {
            return gtRecipe.recipe.recipeType;
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
