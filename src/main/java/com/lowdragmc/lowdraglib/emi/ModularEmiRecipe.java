package com.lowdragmc.lowdraglib.emi;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class ModularEmiRecipe<T extends Widget> implements EmiRecipe {

    public static final List<ModularWrapper<?>> CACHE_OPENED = new ArrayList<>();
    public static ModularWrapper<?> TEMP_CACHE;

    protected Supplier<T> widget;
    protected List<EmiIngredient> inputs = new ArrayList<>();
    protected List<EmiStack> outputs = new ArrayList<>();
    protected List<EmiIngredient> catalysts = new ArrayList<>();
    protected int width;
    protected int height;

    public ModularEmiRecipe(Supplier<T> widget) {
        this.widget = widget;
        var sample = widget.get();
        this.width = sample.getSize().width;
        this.height = sample.getSize().height;
        collectIngredients(sample);
    }

    public List<Widget> getFlatWidgetCollection(T widget) {
        if (widget instanceof WidgetGroup group) {
            var widgets = new ArrayList<Widget>();
            widgets.add(widget);
            widgets.addAll(group.getContainedWidgets(true));
            return widgets;
        }
        return List.of(widget);
    }

    @Override
    public int getDisplayWidth() {
        return width;
    }

    @Override
    public int getDisplayHeight() {
        return height;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        var wrapper = new ModularWrapper<>(widget.get());
        wrapper.setEmiRecipeWidget(0, 0);
        TEMP_CACHE = wrapper;
        CACHE_OPENED.add(wrapper);
        widgets.add(new ModularWrapperWidget(wrapper, List.of()));
        widgets.add(new ModularForegroundRenderWidget(wrapper));
    }

    public void clearSlotWidgetHandler(SlotWidget slotWidget, int index) {}

    public void clearTankWidgetHandler(Widget tankWidget) {}

    public void addTempWidgets(WidgetHolder widgets) {
        if (TEMP_CACHE != null) {
            widgets.add(new ModularWrapperWidget(TEMP_CACHE, List.of()));
            widgets.add(new ModularForegroundRenderWidget(TEMP_CACHE));
        }
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return catalysts;
    }

    private void collectIngredients(T root) {
        for (Widget widget : getFlatWidgetCollection(root)) {
            if (!(widget instanceof IRecipeIngredientSlot slot)) {
                continue;
            }
            var io = slot.getIngredientIO();
            if (io == null || io == IngredientIO.RENDER_ONLY) {
                continue;
            }
            for (Object ingredient : slot.getXEIIngredients()) {
                if (ingredient instanceof EmiStack stack) {
                    if (io == IngredientIO.OUTPUT) {
                        outputs.add(stack);
                    } else if (io == IngredientIO.CATALYST) {
                        catalysts.add(stack);
                    } else {
                        inputs.add(stack);
                    }
                } else if (ingredient instanceof EmiIngredient emiIngredient) {
                    if (io == IngredientIO.OUTPUT) {
                        outputs.addAll(emiIngredient.getEmiStacks());
                    } else if (io == IngredientIO.CATALYST) {
                        catalysts.add(emiIngredient);
                    } else {
                        inputs.add(emiIngredient);
                    }
                }
            }
        }
    }
}
