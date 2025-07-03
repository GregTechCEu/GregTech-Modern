package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class EnergyInfoPeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:energy_info";
    }

    @LuaFunction
    public static MethodResult getEnergyStored(IEnergyInfoProvider infoProvider) {
        return MethodResult.of(infoProvider.getEnergyInfo().stored());
    }

    @LuaFunction
    public static MethodResult getEnergyCapacity(IEnergyInfoProvider infoProvider) {
        return MethodResult.of(infoProvider.getEnergyInfo().capacity());
    }

    @LuaFunction
    public static MethodResult getInputPerSec(IEnergyInfoProvider changeProvider) {
        return MethodResult.of(changeProvider.getInputPerSec());
    }

    @LuaFunction
    public static MethodResult getOutputPerSec(IEnergyInfoProvider changeProvider) {
        return MethodResult.of(changeProvider.getOutputPerSec());
    }
}
