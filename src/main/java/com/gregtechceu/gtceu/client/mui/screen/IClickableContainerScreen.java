package com.gregtechceu.gtceu.client.mui.screen;

import net.minecraft.world.inventory.Slot;

public interface IClickableContainerScreen {

    void gtceu$setClickedSlot(Slot slot);

    Slot gtceu$getClickedSlot();
}
