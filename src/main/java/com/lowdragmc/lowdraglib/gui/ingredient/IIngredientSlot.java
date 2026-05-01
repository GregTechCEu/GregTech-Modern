package com.lowdragmc.lowdraglib.gui.ingredient;

public interface IIngredientSlot {

    default Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        return null;
    }
}
