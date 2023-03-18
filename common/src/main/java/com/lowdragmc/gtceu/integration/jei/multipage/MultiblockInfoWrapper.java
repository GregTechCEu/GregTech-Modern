package com.lowdragmc.gtceu.integration.jei.multipage;

import com.lowdragmc.gtceu.api.gui.widget.PatternPreviewWidget;
import com.lowdragmc.gtceu.api.machine.MultiblockMachineDefinition;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class MultiblockInfoWrapper extends ModularWrapper<PatternPreviewWidget> {
    public final MultiblockMachineDefinition definition;

    public MultiblockInfoWrapper(MultiblockMachineDefinition definition) {
        super(PatternPreviewWidget.getPatternWidget(definition));
        this.definition = definition;
    }

}
