package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.common.machine.multiblock.generator.LargeTurbineMachine;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class TurbineMachinePeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:large_turbine";
    }

    @LuaFunction
    public static MethodResult hasRotor(LargeTurbineMachine turbine) {
        return MethodResult.of(turbine.hasRotor());
    }

    @LuaFunction
    public static MethodResult getRotorSpeed(LargeTurbineMachine turbine) {
        return MethodResult.of(turbine.getRotorSpeed());
    }

    @LuaFunction
    public static MethodResult getMaxRotorHolderSpeed(LargeTurbineMachine turbine) {
        return MethodResult.of(turbine.getMaxRotorHolderSpeed());
    }

    @LuaFunction
    public static MethodResult getTotalEfficiency(LargeTurbineMachine turbine) {
        return MethodResult.of(turbine.getTotalEfficiency());
    }

    @LuaFunction
    public static MethodResult getCurrentProduction(LargeTurbineMachine turbine) {
        return MethodResult.of(turbine.getCurrentProduction());
    }

    @LuaFunction
    public static MethodResult getOverclockVoltage(LargeTurbineMachine turbine) {
        return MethodResult.of(turbine.getOverclockVoltage());
    }

    @LuaFunction
    public static MethodResult getRotorDurabilityPercent(LargeTurbineMachine turbine) {
        return MethodResult.of(turbine.getRotorDurabilityPercent());
    }
}
