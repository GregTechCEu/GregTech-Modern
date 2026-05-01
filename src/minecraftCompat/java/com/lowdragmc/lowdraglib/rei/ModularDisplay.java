package com.lowdragmc.lowdraglib.rei;

import com.lowdragmc.lowdraglib.gui.ingredient.IRecipeIngredientSlot;
import com.lowdragmc.lowdraglib.gui.widget.SlotWidget;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.jei.IngredientIO;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.entry.EntryIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ModularDisplay<T extends Widget> implements Display {

    public static final List<ModularWrapper<?>> CACHE_OPENED = new ArrayList<>();

    protected Supplier<T> widget;
    protected List<EntryIngredient> inputs = new ArrayList<>();
    protected List<EntryIngredient> outputs = new ArrayList<>();
    protected List<EntryIngredient> catalysts = new ArrayList<>();
    protected final CategoryIdentifier<?> category;

    public ModularDisplay(Supplier<T> widget, CategoryIdentifier<?> category) {
        this.widget = widget;
        this.category = category;
        collectIngredients(widget.get());
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

    public List<me.shedaniel.rei.api.client.gui.widgets.Widget> createWidget(Rectangle bounds) {
        var modular = new ModularWrapper<>(widget.get());
        modular.setRecipeWidget(bounds.x + 4, bounds.y + 4);
        CACHE_OPENED.add(modular);
        return List.of(Widgets.createDrawableWidget(
                (graphics, mouseX, mouseY, delta) -> modular.render(graphics, mouseX, mouseY, delta)));
    }

    public void clearSlotWidgetHandler(SlotWidget slotWidget, int index) {}

    public void clearTankWidgetHandler(Widget tankWidget) {}

    @Override
    public List<EntryIngredient> getInputEntries() {
        return inputs;
    }

    @Override
    public List<EntryIngredient> getOutputEntries() {
        return outputs;
    }

    @Override
    public List<EntryIngredient> getRequiredEntries() {
        return inputs;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return category;
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
                if (ingredient instanceof EntryIngredient entryIngredient) {
                    if (io == IngredientIO.OUTPUT) {
                        outputs.add(entryIngredient);
                    } else if (io == IngredientIO.CATALYST) {
                        catalysts.add(entryIngredient);
                    } else {
                        inputs.add(entryIngredient);
                    }
                }
            }
        }
    }
}
