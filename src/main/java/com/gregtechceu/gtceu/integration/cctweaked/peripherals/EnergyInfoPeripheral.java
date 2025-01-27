package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.capability.IEnergyInfoProvider;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

import java.math.BigInteger;

public class EnergyInfoPeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:energy_info";
    }

    @LuaFunction
    static public MethodResult getEnergyStored(IEnergyInfoProvider infoProvider) {
        return toResult(infoProvider.getEnergyInfo().stored());
    }

    @LuaFunction
    static public MethodResult getEnergyCapacity(IEnergyInfoProvider infoProvider) {
        return toResult(infoProvider.getEnergyInfo().capacity());
    }

    private static BigInteger MAX_LONG = BigInteger.valueOf(Long.MAX_VALUE);

    private static MethodResult toResult(BigInteger val) {
        return MethodResult.of(val);
    }
}
