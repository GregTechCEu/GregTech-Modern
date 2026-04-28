package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualItemStorage;

import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;

final class EnderItemLinkCoverUI {

    private EnderItemLinkCoverUI() {}

    static Object addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height, boolean canClick) {
        WidgetGroup group = new WidgetGroup(x, y, width, height);
        for (int i = 0; i < ((VirtualItemStorage) entry).getHandler().getSlots(); i++) {
            group.addWidget(new SlotWidget(((VirtualItemStorage) entry).getHandler(), i, 8 * i, 0, canClick, canClick));
        }
        return group;
    }
}
