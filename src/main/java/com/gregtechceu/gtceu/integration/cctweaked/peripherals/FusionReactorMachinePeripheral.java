package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.common.machine.multiblock.electric.FusionReactorMachine;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class FusionReactorMachinePeripheral implements GenericPeripheral {

    public String id() { return "gtceu:fusion_reactor"; }

    @LuaFunction
    public static MethodResult getHeat(FusionReactorMachine reactor) {
        return MethodResult.of(reactor.getHeat());
    }

}
