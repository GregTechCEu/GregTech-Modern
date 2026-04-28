package com.lowdragmc.lowdraglib.gui.ingredient;

import net.minecraft.client.renderer.Rect2i;

public interface Target extends java.util.function.Consumer<Object> {

    Rect2i getArea();

    @Override
    void accept(Object ingredient);
}
