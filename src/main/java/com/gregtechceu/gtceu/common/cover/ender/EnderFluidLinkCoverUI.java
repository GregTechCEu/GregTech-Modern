package com.gregtechceu.gtceu.common.cover.ender;

import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.misc.virtualregistry.VirtualEntry;
import com.gregtechceu.gtceu.api.misc.virtualregistry.entries.VirtualTank;

final class EnderFluidLinkCoverUI {

    private EnderFluidLinkCoverUI() {}

    static Object addVirtualEntryWidget(VirtualEntry entry, int x, int y, int width, int height, boolean canClick) {
        return new TankWidget(((VirtualTank) entry).getFluidTank(), 0, x, y, width, height, canClick, canClick)
                .setBackground(GuiTextures.FLUID_SLOT);
    }
}
