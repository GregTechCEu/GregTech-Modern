package com.lowdragmc.lowdraglib.rei;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;

import java.util.List;

public abstract class ModularUIDisplayCategory<T extends ModularDisplay<?>> implements DisplayCategory<T> {

    @Override
    public List<me.shedaniel.rei.api.client.gui.widgets.Widget> setupDisplay(T display, Rectangle bounds) {
        return display.createWidget(bounds);
    }
}
