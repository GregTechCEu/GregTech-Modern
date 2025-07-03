package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.capability.IEnergyChangeProvider;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class EnergyChangePeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:energy_change";
    }

    @LuaFunction
    public static MethodResult getAverageInLastSec(IEnergyChangeProvider changeProvider) {
        return MethodResult.of(changeProvider.getEnergyChange().averageInLastSec());
    }

    @LuaFunction
    public static MethodResult getAverageOutLastSec(IEnergyChangeProvider changeProvider) {
        return MethodResult.of(changeProvider.getEnergyChange().averageOutLastSec());
    }
}
