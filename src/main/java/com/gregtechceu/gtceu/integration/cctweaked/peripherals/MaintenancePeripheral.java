package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.common.machine.multiblock.part.ConfigurableMaintenanceHatchPartMachine;
import com.gregtechceu.gtceu.common.machine.multiblock.part.MaintenanceHatchPartMachine;

import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.lua.MethodResult;
import dan200.computercraft.api.peripheral.GenericPeripheral;

public class MaintenancePeripheral implements GenericPeripheral {

    @Override
    public String id() {
        return "gtceu:maintenance";
    }

    @LuaFunction
    public static MethodResult hasMaintenanceProblems(MaintenanceHatchPartMachine maintenanceHatchPartMachine) {
        return MethodResult.of(maintenanceHatchPartMachine.hasMaintenanceProblems());
    }

    @LuaFunction
    public static MethodResult getMaintenanceProblems(MaintenanceHatchPartMachine maintenanceHatchPartMachine) {
        return MethodResult.of(maintenanceHatchPartMachine.getMaintenanceProblems());
    }

    @LuaFunction
    public static MethodResult getNumMaintenanceProblems(MaintenanceHatchPartMachine maintenanceHatchPartMachine) {
        return MethodResult.of(maintenanceHatchPartMachine.getNumMaintenanceProblems());
    }

    @LuaFunction
    public static MethodResult getDurationMultiplier(ConfigurableMaintenanceHatchPartMachine configurableMaintenanceHatchPartMachine) {
        return MethodResult.of(configurableMaintenanceHatchPartMachine.getDurationMultiplier());
    }

    @LuaFunction
    public static MethodResult setDurationMultiplier(ConfigurableMaintenanceHatchPartMachine configurableMaintenanceHatchPartMachine,
                                                     int multiplier) {
        configurableMaintenanceHatchPartMachine.setDurationMultiplier(multiplier);
        return MethodResult.of();
    }

    @LuaFunction
    public static MethodResult isTaped(MaintenanceHatchPartMachine maintenanceHatchPartMachine) {
        return MethodResult.of(maintenanceHatchPartMachine.isTaped());
    }
}
