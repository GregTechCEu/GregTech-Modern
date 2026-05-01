package com.lowdragmc.lowdraglib.gui.ingredient;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.jei.IngredientIO;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface IRecipeIngredientSlot extends IIngredientSlot {

    default Widget self() {
        return (Widget) this;
    }

    List<Object> getXEIIngredients();

    default Object getXEICurrentIngredient() {
        var ingredients = getXEIIngredients();
        return ingredients.isEmpty() ? null : ingredients.get(0);
    }

    default float getXEIChance() {
        return 1.0f;
    }

    default IngredientIO getIngredientIO() {
        return IngredientIO.RENDER_ONLY;
    }

    default List<Component> getFullTooltipTexts() {
        return List.of();
    }
}
