package com.gregtechceu.gtceu.api.mui.base.widget;

import net.minecraft.world.inventory.Slot;

/**
 * Marks a {@link IWidget} as containing a vanilla item slot.
 */
public interface IVanillaSlot {

    /**
     * @return the item slot of this widget
     */
    Slot getVanillaSlot();

    boolean handleAsVanillaSlot();
}
