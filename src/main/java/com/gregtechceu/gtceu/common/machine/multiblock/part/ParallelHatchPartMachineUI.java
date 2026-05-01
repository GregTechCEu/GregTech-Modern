package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.gui.widget.IntInputWidget;

import com.lowdragmc.lowdraglib.gui.widget.Widget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class ParallelHatchPartMachineUI {

    private ParallelHatchPartMachineUI() {}

    static Widget createUIWidget(ParallelHatchPartMachine machine) {
        WidgetGroup parallelAmountGroup = new WidgetGroup(0, 0, 100, 20);
        parallelAmountGroup.addWidget(new IntInputWidget(machine::getCurrentParallel, machine::setCurrentParallel)
                .setMin(ParallelHatchPartMachine.MIN_PARALLEL)
                .setMax(machine.maxParallel));
        return parallelAmountGroup;
    }
}
