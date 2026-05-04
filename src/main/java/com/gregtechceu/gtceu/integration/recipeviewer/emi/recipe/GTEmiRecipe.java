package com.gregtechceu.gtceu.integration.recipeviewer.emi.recipe;

import brachy.modularui.integration.emi.recipe.ModularUIEmiRecipe;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.gui.GTRecipeViewerWidget;

import dev.emi.emi.api.stack.EmiStack;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;

import java.util.List;

public class GTEmiRecipe extends ModularUIEmiRecipe {

    final EmiRecipeCategory category;
    final GTRecipe recipe;

    public GTEmiRecipe(GTRecipe recipe, EmiRecipeCategory category) {
        super(recipe.getId(), () -> new GTRecipeViewerWidget(recipe));
        this.category = category;
        this.recipe = recipe;

    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    //    @Override
//    public void addWidgets(WidgetHolder widgets) {
//        var widget = this.widget.get();
//        var modular = new ModularWrapper<>(widget);
//        modular.setRecipeWidget(0, 0);
//
//        synchronized (CACHE_OPENED) {
//            CACHE_OPENED.add(modular);
//        }
//        List<Widget> slots = new ArrayList<>();
//        for (com.lowdragmc.lowdraglib.gui.widget.Widget w : getFlatWidgetCollection(widget)) {
//            if (w instanceof IRecipeIngredientSlot slot) {
//                if (w.getParent() instanceof DraggableScrollableWidgetGroup draggable && draggable.isUseScissor()) {
//                    // don't add the EMI widget at all if we have a draggable group, let the draggable widget handle it
//                    // instead.
//                    continue;
//                }
//                var io = slot.getIngredientIO();
//                if (io != null && io != IngredientIO.RENDER_ONLY) {
//                    // noinspection unchecked
//                    var ingredients = EmiIngredient
//                            .of((List<? extends EmiIngredient>) (List<?>) slot.getXEIIngredients());
//
//                    SlotWidget slotWidget = null;
//                    // Clear the LDLib slots & add EMI slots based on them.
//                    if (slot instanceof com.gregtechceu.gtceu.api.gui.widget.SlotWidget slotW) {
//                        slotW.setHandlerSlot((IItemHandlerModifiable) EmptyHandler.INSTANCE, 0);
//                        slotW.setDrawHoverOverlay(false).setDrawHoverTips(false);
//                    } else if (slot instanceof com.gregtechceu.gtceu.api.gui.widget.TankWidget tankW) {
//                        tankW.setFluidTank(EmptyFluidHandler.INSTANCE);
//                        tankW.setDrawHoverOverlay(false).setDrawHoverTips(false);
//                        long capacity = Math.max(1, ingredients.getAmount());
//                        slotWidget = new TankWidget(ingredients, w.getPosition().x, w.getPosition().y,
//                                w.getSize().width, w.getSize().height, capacity);
//                    }
//                    if (slotWidget == null) {
//                        slotWidget = new SlotWidget(ingredients, w.getPosition().x, w.getPosition().y);
//                    }
//
//                    slotWidget
//                            .customBackground(null, w.getPosition().x, w.getPosition().y, w.getSize().width,
//                                    w.getSize().height)
//                            .drawBack(false);
//                    if (io == IngredientIO.CATALYST) {
//                        slotWidget.catalyst(true);
//                    } else if (io == IngredientIO.OUTPUT) {
//                        slotWidget.recipeContext(this);
//                    }
//                    for (Component component : w.getTooltipTexts()) {
//                        slotWidget.appendTooltip(component);
//                    }
//                    slots.add(slotWidget);
//                }
//            }
//        }
//        widgets.add(new ModularWrapperWidget(modular, slots));
//        slots.forEach(widgets::add);
//        widgets.add(new ModularForegroundRenderWidget(modular));
//    }
}
