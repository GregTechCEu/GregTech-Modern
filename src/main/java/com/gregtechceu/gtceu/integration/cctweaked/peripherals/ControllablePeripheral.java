package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.capability.IControllable;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class ControllablePeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:workable";
    }

    @LuaFunction
    public static MethodResult isWorkingEnabled(IControllable controllable) {
        return MethodResult.of(controllable.isWorkingEnabled());
    }

    @LuaFunction
    public static void setWorkingEnabled(IControllable controllable, boolean enabled) {
        controllable.setWorkingEnabled(enabled);
    }

    @LuaFunction
    public static void setSuspendAfterFinish(IControllable controllable, boolean suspendAfterFinish) {
        controllable.setSuspendAfterFinish(suspendAfterFinish);
    }
}
