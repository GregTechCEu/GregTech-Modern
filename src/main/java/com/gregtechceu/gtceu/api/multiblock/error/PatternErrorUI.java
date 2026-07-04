package com.gregtechceu.gtceu.api.multiblock.error;

import brachy.modularui.widget.ParentWidget;

@FunctionalInterface
public interface PatternErrorUI {

    void apply(ParentWidget<?> parent);
}
