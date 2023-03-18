package com.lowdragmc.gtceu.integration.rei.multipage;


import com.lowdragmc.gtceu.api.gui.widget.PatternPreviewWidget;
import com.lowdragmc.gtceu.api.machine.MultiblockMachineDefinition;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.rei.ModularDisplay;

public class MultiblockInfoDisplay extends ModularDisplay<WidgetGroup> {
    public final MultiblockMachineDefinition definition;

    public MultiblockInfoDisplay(MultiblockMachineDefinition definition) {
        super(() -> PatternPreviewWidget.getPatternWidget(definition), MultiblockInfoDisplayCategory.CATEGORY);
        this.definition = definition;
    }

}
