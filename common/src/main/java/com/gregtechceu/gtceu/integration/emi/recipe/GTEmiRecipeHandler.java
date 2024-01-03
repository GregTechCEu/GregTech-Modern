package com.gregtechceu.gtceu.integration.emi.recipe;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.world.inventory.Slot;

import java.util.List;

public class GTEmiRecipeHandler implements StandardRecipeHandler<ModularUIContainer> {
    @Override
    public List<Slot> getInputSources(ModularUIContainer handler) {
        return handler.getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT || e.isPlayerContainer)
                .map(SlotWidget::getHandle)
                .toList();
    }

    @Override
    public List<Slot> getCraftingSlots(ModularUIContainer handler) {
        return handler.getModularUI().getSlotMap().values().stream()
                .filter(e -> e.getIngredientIO() == IngredientIO.INPUT)
                .map(SlotWidget::getHandle)
                .toList();
    }


    @Override
    public boolean supportsRecipe(EmiRecipe recipe) {
        return recipe instanceof GTEmiRecipe;
    }
}
