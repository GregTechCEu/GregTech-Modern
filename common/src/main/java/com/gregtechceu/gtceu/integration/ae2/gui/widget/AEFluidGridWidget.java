package com.gregtechceu.gtceu.integration.ae2.gui.widget;

import appeng.api.behaviors.GenericInternalInventory;
import appeng.api.stacks.GenericStack;
import com.lowdragmc.lowdraglib.gui.widget.Widget;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

/**
 * @Author GlodBlock
 * @Description Display fluid list
 * @Date 2023/4/19-0:28
 */
public class AEFluidGridWidget extends AEListGridWidget {

    public AEFluidGridWidget(int x, int y, int slotsY, GenericInternalInventory internalList) {
        super(x, y, slotsY, internalList);
    }

    @Override
    protected void addSlotRows(int amount) {
        for (int i = 0; i < amount; i++) {
            int widgetAmount = this.widgets.size();
            Widget widget = new AEFluidDisplayWidget(0, 0, this, widgetAmount);
            this.addWidget(widget);
        }
    }
}
