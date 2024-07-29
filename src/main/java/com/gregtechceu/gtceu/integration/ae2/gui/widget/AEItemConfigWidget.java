package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import com.gregtechceu.gtceu.integration.ae2.gui.widget.slot.AEItemConfigSlotWidget;
import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.slot.ExportOnlyAEItemSlot;
import com.gregtechceu.gtceu.integration.ae2.slot.IConfigurableSlot;

/**
 * @Author GlodBlock
 * @Description Display {@link net.minecraft.world.item.ItemStack} config
 * @Date 2023/4/22-1:02
 */
public class AEItemConfigWidget extends ConfigWidget {

    public AEItemConfigWidget(int x, int y, IConfigurableSlot[] config) {
        super(x, y, config);
    }

    @Override
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index++) {
            this.displayList[index] = new ExportOnlyAEItemSlot();
            this.cached[index] = new ExportOnlyAEItemSlot();
            line = index / 8;
            this.addWidget(new AEItemConfigSlotWidget((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
    }
}
