package com.gregtechceu.gtceu.integration.jei.multipage;

import com.gregtechceu.gtceu.api.gui.widget.PatternPreviewWidget;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;

import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class MultiblockInfoWrapper extends ModularWrapper<PatternPreviewWidget> {

    public final MultiblockMachineDefinition definition;

    public MultiblockInfoWrapper(MultiblockMachineDefinition definition) {
        super(PatternPreviewWidget.getPatternWidget(definition));
        this.definition = definition;
    }
}
