package com.gregtechceu.gtceu.integration.cctweaked;

import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.integration.cctweaked.peripherals.*;

import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.ForgeComputerCraftAPI;

public class CCTweakedPlugin {

    public static void init() {
        ComputerCraftAPI.registerGenericSource(new EnergyInfoPeripheral());
        ForgeComputerCraftAPI.registerGenericCapability(GTCapability.CAPABILITY_ENERGY_INFO_PROVIDER);
    }
}
