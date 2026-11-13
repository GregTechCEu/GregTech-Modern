package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.widgets.SliderWidget;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.testmultis.TieredWorkableMultiMachine;

public class TestMultiPreviewWidget extends MultiblockPreviewWidget {
    public TestMultiPreviewWidget(TieredWorkableMultiMachine machine, MultiblockSchemaInfo schemaInfo, int width, int height, boolean terminal) {
        super(machine.getDefinition(), schemaInfo, width, height, terminal);

        child(new SliderWidget()
                .value(new DoubleSyncValue(() -> machine.getTier(), (v) -> machine.setTier((int)v))));
    }
}
