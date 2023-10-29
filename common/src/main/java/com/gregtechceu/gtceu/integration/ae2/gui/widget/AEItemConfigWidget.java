package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import com.gregtechceu.gtceu.integration.ae2.machine.MEInputBusPartMachine;
import com.gregtechceu.gtceu.integration.ae2.util.AEItemConfigSlot;
import com.gregtechceu.gtceu.integration.ae2.util.IConfigurableSlot;

/**
 * @Author GlodBlock
 * @Description Display {@link net.minecraft.world.item.ItemStack} config
 * @Date 2023/4/22-1:02
 */
public class AEItemConfigWidget extends AEConfigWidget {

    public AEItemConfigWidget(int x, int y, IConfigurableSlot[] config) {
        super(x, y, config);
    }

    @Override
    void init() {
        int line;
        this.displayList = new IConfigurableSlot[this.config.length];
        this.cached = new IConfigurableSlot[this.config.length];
        for (int index = 0; index < this.config.length; index ++) {
            this.displayList[index] = new MEInputBusPartMachine.ExportOnlyAEItem();
            this.cached[index] = new MEInputBusPartMachine.ExportOnlyAEItem();
            line = index / 8;
            this.addWidget(new AEItemConfigSlot((index - line * 8) * 18, line * (18 * 2 + 2), this, index));
        }
    }

}