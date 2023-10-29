package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import com.gregtechceu.gtceu.integration.ae2.machine.MEInputHatchPartMachine;
import com.gregtechceu.gtceu.integration.ae2.util.AEFluidConfigSlot;
import com.gregtechceu.gtceu.integration.ae2.util.IConfigurableSlot;

/**
 * @Author GlodBlock
 * @Description Display {@link com.lowdragmc.lowdraglib.side.fluid.FluidStack} config
 * @Date 2023/4/21-1:45
 */
public class AEFluidConfigWidget extends AEConfigWidget {

    public AEFluidConfigWidget(int x, int y, IConfigurableSlot[] config) {
        super(x, y, config);
    }

    @Override
    @SuppressWarnings("unchecked")
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index ++) {
            this.displayList[index] = new MEInputHatchPartMachine.ExportOnlyAEFluid();
            this.cached[index] = new MEInputHatchPartMachine.ExportOnlyAEFluid();
            line = index / 8;
            this.addWidget(new AEFluidConfigSlot((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
    }
}