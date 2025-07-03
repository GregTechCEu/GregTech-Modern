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
    public static MethodResult getAverageInputLastSec(IEnergyChangeProvider changeProvider) {
        return MethodResult.of(changeProvider.getAverageInputLastSec());
    }

    @LuaFunction
    public static MethodResult getAverageOutputLastSec(IEnergyChangeProvider changeProvider) {
        return MethodResult.of(changeProvider.getAverageOutputLastSec());
    }
}
