package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.capability.ITurbineMachine;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class TurbineMachinePeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:large_turbine";
    }

    @LuaFunction
    public static MethodResult hasRotor(ITurbineMachine turbine) {
        return MethodResult.of(turbine.hasRotor());
    }

    @LuaFunction
    public static MethodResult getRotorSpeed(ITurbineMachine turbine) {
        return MethodResult.of(turbine.getRotorSpeed());
    }

    @LuaFunction
    public static MethodResult getMaxRotorHolderSpeed(ITurbineMachine turbine) {
        return MethodResult.of(turbine.getMaxRotorHolderSpeed());
    }

    @LuaFunction
    public static MethodResult getTotalEfficiency(ITurbineMachine turbine) {
        return MethodResult.of(turbine.getTotalEfficiency());
    }

    @LuaFunction
    public static MethodResult getCurrentProduction(ITurbineMachine turbine) {
        return MethodResult.of(turbine.getCurrentProduction());
    }

    @LuaFunction
    public static MethodResult getOverclockVoltage(ITurbineMachine turbine) {
        return MethodResult.of(turbine.getOverclockVoltage());
    }

    @LuaFunction
    public static MethodResult getRotorDurabilityPercent(ITurbineMachine turbine) {
        return MethodResult.of(turbine.getRotorDurabilityPercent());
    }
}
