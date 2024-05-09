package com.gregtechceu.gtceu.integration.jei.multipage;

import com.gregtechceu.gtceu.api.machines.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.guis.widget.PatternPreviewWidget;
import com.lowdragmc.lowdraglib.jei.ModularWrapper;

public class MultiblockInfoWrapper extends ModularWrapper<PatternPreviewWidget> {
    public final MultiblockMachineDefinition definition;

    public MultiblockInfoWrapper(MultiblockMachineDefinition definition) {
        super(PatternPreviewWidget.getPatternWidget(definition));
        this.definition = definition;
    }

}
