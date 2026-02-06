package com.gregtechceu.gtceu.api.mui.base.widget;

import net.minecraft.world.inventory.Slot;

public interface IDelegatingWidget extends IWidget, IVanillaSlot {

    IWidget getDelegate();

    @Override
    default Slot getVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot ? vanillaSlot.getVanillaSlot() : null;
    }

    @Override
    default boolean handleAsVanillaSlot() {
        return getDelegate() instanceof IVanillaSlot vanillaSlot && vanillaSlot.handleAsVanillaSlot();
    }
}
