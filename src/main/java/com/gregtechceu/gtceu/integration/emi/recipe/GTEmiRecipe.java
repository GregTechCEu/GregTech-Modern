package com.gregtechceu.gtceu.integration.emi.recipe;

import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.recipe.GTRecipeDefinition;
import com.gregtechceu.gtceu.api.recipe.content.ContentListMap;
import com.gregtechceu.gtceu.integration.xei.widgets.GTRecipeWidget;

import com.lowdragmc.lowdraglib.emi.ModularEmiRecipe;
import com.lowdragmc.lowdraglib.emi.ModularForegroundRenderWidget;
import com.lowdragmc.lowdraglib.emi.ModularWrapperWidget;
import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fluids.capability.templates.EmptyFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.EmptyHandler;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class GTEmiRecipe extends ModularEmiRecipe<WidgetGroup> {

    final EmiRecipeCategory category;
    final GTRecipeDefinition recipe;

    public GTEmiRecipe(GTRecipeDefinition recipe, EmiRecipeCategory category) {
        super(() -> GTRecipeWidget.getPlaceHolder(recipe));
        this.category = category;
        this.recipe = recipe;
        this.inputs = getEmiIngredients(IO.IN, recipe.inputs, recipe.tickInputs);
        this.outputs = getEmiStacks(IO.OUT, recipe.outputs, recipe.tickOutputs);
        for(var condition: recipe.conditions) {
            if(!condition.hasXEICatalysts()) return;
            this.catalysts.addAll((List<EmiIngredient>) condition.getXEICatalysts());
        }
        this.widget = () -> new GTRecipeWidget(recipe);
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var widget = this.widget.get();
        var modular = new ModularWrapper<>(widget);
        modular.setRecipeWidget(0, 0);

        synchronized (CACHE_OPENED) {
            CACHE_OPENED.add(modular);
        }
        List<Widget> slots = new ArrayList<>();
        for (com.lowdragmc.lowdraglib.gui.widget.Widget w : getFlatWidgetCollection(widget)) {
            if (w instanceof IRecipeIngredientSlot slot) {
                if (w.getParent() instanceof DraggableScrollableWidgetGroup draggable && draggable.isUseScissor()) {
                    // don't add the EMI widget at all if we have a draggable group, let the draggable widget handle it
                    // instead.
                    continue;
                }
                var io = slot.getIngredientIO();
                if (io != null && io != IngredientIO.RENDER_ONLY) {
                    // noinspection unchecked
                    var ingredients = EmiIngredient
                            .of((List<? extends EmiIngredient>) (List<?>) slot.getXEIIngredients());

                    SlotWidget slotWidget = null;
                    // Clear the LDLib slots & add EMI slots based on them.
                    if (slot instanceof com.gregtechceu.gtceu.api.gui.widget.SlotWidget slotW) {
                        slotW.setHandlerSlot((IItemHandlerModifiable) EmptyHandler.INSTANCE, 0);
                        slotW.setDrawHoverOverlay(false).setDrawHoverTips(false);
                    } else if (slot instanceof com.gregtechceu.gtceu.api.gui.widget.TankWidget tankW) {
                        tankW.setFluidTank(EmptyFluidHandler.INSTANCE);
                        tankW.setDrawHoverOverlay(false).setDrawHoverTips(false);
                        long capacity = Math.max(1, ingredients.getAmount());
                        slotWidget = new TankWidget(ingredients, w.getPosition().x, w.getPosition().y,
                                w.getSize().width, w.getSize().height, capacity);
                    }
                    if (slotWidget == null) {
                        slotWidget = new SlotWidget(ingredients, w.getPosition().x, w.getPosition().y);
                    }

                    slotWidget
                            .customBackground(null, w.getPosition().x, w.getPosition().y, w.getSize().width,
                                    w.getSize().height)
                            .drawBack(false);
                    if (io == IngredientIO.CATALYST) {
                        slotWidget.catalyst(true);
                    } else if (io == IngredientIO.OUTPUT) {
                        slotWidget.recipeContext(this);
                    }
                    for (Component component : w.getTooltipTexts()) {
                        slotWidget.appendTooltip(component);
                    }
                    slots.add(slotWidget);
                }
            }
        }
        widgets.add(new ModularWrapperWidget(modular, slots));
        slots.forEach(widgets::add);
        widgets.add(new ModularForegroundRenderWidget(modular));
    }

    private List<EmiIngredient> getEmiIngredients(IO io, ContentListMap... contentListMaps) {
        List<EmiIngredient> list = new ArrayList<>();
        for(var contentListMap : contentListMaps){
            contentListMap.forEachEntry(new ContentListMap.EntryConsumer() {
                @Override
                public <T> void accept(RecipeCapability<T> capability, List<T> contents) {
                    list.addAll((List<EmiIngredient>)capability.getXEIIngredients(contents, recipe, io));
                }
            });
        }
        return list;
    }

    private List<EmiStack> getEmiStacks(IO io, ContentListMap... contentListMaps) {
        List<EmiStack> list = new ArrayList<>();
        for(var contentListMap : contentListMaps){
            contentListMap.forEachEntry(new ContentListMap.EntryConsumer() {
                @Override
                public <T> void accept(RecipeCapability<T> capability, List<T> contents) {
                    ((List<EmiIngredient>)capability.getXEIIngredients(contents, recipe, io))
                            .stream()
                            .map(ingredient -> ingredient.getEmiStacks().get(0))
                            .forEach(list::add);

                }
            });
        }
        return list;
    }
}
