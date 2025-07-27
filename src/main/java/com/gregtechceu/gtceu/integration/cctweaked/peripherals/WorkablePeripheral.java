package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.capability.IWorkable;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class WorkablePeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:workable";
    }

    @LuaFunction
    public static MethodResult getProgress(IWorkable workable) {
        return MethodResult.of(workable.getProgress());
    }

    @LuaFunction
    public static MethodResult getMaxProgress(IWorkable workable) {
        return MethodResult.of(workable.getMaxProgress());
    }

    @LuaFunction
    public static MethodResult isActive(IWorkable workable) {
        return MethodResult.of(workable.isActive());
    }
}
