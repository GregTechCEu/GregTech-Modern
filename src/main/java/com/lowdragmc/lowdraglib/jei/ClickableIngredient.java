package com.lowdragmc.lowdraglib.jei;

import net.minecraft.client.renderer.Rect2i;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IClickableIngredient;

public class ClickableIngredient<T> implements IClickableIngredient<T> {

    private final ITypedIngredient<T> typedIngredient;
    private final Rect2i area;

    public ClickableIngredient(ITypedIngredient<T> typedIngredient, int x, int y, int width, int height) {
        this.typedIngredient = typedIngredient;
        this.area = new Rect2i(x, y, width, height);
    }

    @Override
    public ITypedIngredient<T> getTypedIngredient() {
        return typedIngredient;
    }

    @Override
    public Rect2i getArea() {
        return area;
    }
}
