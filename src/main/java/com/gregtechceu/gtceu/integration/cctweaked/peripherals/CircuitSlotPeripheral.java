package com.gregtechceu.gtceu.integration.cctweaked.peripherals;

import com.gregtechceu.gtceu.api.machine.feature.IHasCircuitSlot;
import com.gregtechceu.gtceu.common.item.IntCircuitBehaviour;

import net.minecraft.world.item.ItemStack;

import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dan200.computercraft.api.peripheral.GenericPeripheral;

import java.util.Optional;

import javax.annotation.Nullable;

public class CircuitSlotPeripheral implements GenericPeripheral {

    public String id() {
        return "gtceu:circuit_slot";
    }

    @LuaFunction
    public static @Nullable Integer getProgrammedCircuit(IHasCircuitSlot hasCircuitSlot) {
        ItemStack circuitStack = hasCircuitSlot.getCircuitInventory().getStackInSlot(0);
        if (circuitStack == ItemStack.EMPTY) return null;
        return IntCircuitBehaviour.getCircuitConfiguration(circuitStack);
    }

    // Note: using '@Nullable Integer newValue' as a param causes the function to not be picked up by CC.
    // It needs the Optional<Integer>, despite Java complaining about it.
    @LuaFunction
    public static void setProgrammedCircuit(IHasCircuitSlot hasCircuitSlot,
                                            Optional<Integer> newValue) throws LuaException {
        if (newValue.isEmpty()) {
            hasCircuitSlot.getCircuitInventory().setStackInSlot(0, ItemStack.EMPTY);
        } else {
            if ((newValue.get() < 0) || (newValue.get() > 32)) {
                throw new LuaException("newValue " + newValue.get() + " is not within range 0..32 or nil");
            }
            hasCircuitSlot.getCircuitInventory().setStackInSlot(0, IntCircuitBehaviour.stack(newValue.get()));
        }
    }
}
