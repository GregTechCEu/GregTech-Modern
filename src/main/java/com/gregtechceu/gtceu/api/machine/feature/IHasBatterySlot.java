package com.gregtechceu.gtceu.api.machine.feature;

import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

public interface IHasBatterySlot extends IMachineFeature {

    CustomItemStackHandler getChargerInventory();
}
