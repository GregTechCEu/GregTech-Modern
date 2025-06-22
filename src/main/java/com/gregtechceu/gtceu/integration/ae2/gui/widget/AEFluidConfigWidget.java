package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEFluidConfigSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidList;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEFluidSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

import appeng.api.stacks.GenericStack;

public class AEFluidConfigWidget extends ConfigWidget {

    private final ExportOnlyAEFluidList fluidList;

    public AEFluidConfigWidget(int x, int y, ExportOnlyAEFluidList list) {
        super(x, y, list.getInventory(), list.isStocking());
        this.fluidList = list;
    }

    @Override
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new ExportOnlyAEFluidSlot();
            this.cached[index] = new ExportOnlyAEFluidSlot();
            line = index / 8;
            this.addWidget(new AEFluidConfigSlotWidget((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
    }

    public boolean hasStackInConfig(GenericStack stack) {
        return fluidList.hasStackInConfig(stack, true);
    }

    public boolean isAutoPull() {
        return fluidList.isAutoPull();
    }
}
