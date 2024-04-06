package com.gregtechceu.gtceu.integration.emi.recipe;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class GTEmiRecipeHandler implements StandardRecipeHandler<AbstractContainerMenu> {
    @Override
    public List<Slot> getInputSources(AbstractContainerMenu handler) {
        if (!(handler instanceof ModularUIContainer))
            return List.of();

        return ((ModularUIContainer) handler).getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT || e.isPlayerContainer || e.isPlayerHotBar)
                .map(SlotWidget::getHandler)
                .toList();
    }

    @Override
    public List<Slot> getCraftingSlots(AbstractContainerMenu handler) {
        if (!(handler instanceof ModularUIContainer))
            return List.of();

        return ((ModularUIContainer) handler).getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT)
                .map(SlotWidget::getHandler)
                .toList();
    }


    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof GTEmiRecipe;
    }
}
