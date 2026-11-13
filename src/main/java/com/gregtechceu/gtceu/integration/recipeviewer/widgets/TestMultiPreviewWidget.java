package com.gregtechceu.gtceu.integration.recipeviewer.widgets;

import brachy.modularui.value.sync.DoubleSyncValue;
import brachy.modularui.widgets.SliderWidget;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.testmultis.TieredWorkableMultiMachine;

public class TestMultiPreviewWidget extends MultiblockPreviewWidget {
    public TestMultiPreviewWidget(MultiblockControllerMachine machine, MultiblockSchemaInfo schemaInfo, int width, int height, boolean terminal) {
        super(machine.getDefinition(), schemaInfo, width, height, terminal);

        if (!(machine instanceof TieredWorkableMultiMachine tieredMachine)) return;
        child(new SliderWidget().value(new DoubleSyncValue(() -> tieredMachine.getTier(), (v) -> tieredMachine.setTier((int)v))));
    }
}
