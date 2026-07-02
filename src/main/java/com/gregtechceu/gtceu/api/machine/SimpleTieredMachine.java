package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.common.machine.trait.ProgrammableCircuitSlotTrait;
import com.gregtechceu.gtceu.common.data.machines.GTMachineUtils;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.machine.trait.BatterySlotTrait;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.*;

/**
 * All simple single machines are implemented here.
 */
public class SimpleTieredMachine extends WorkableTieredMachine {

    public SimpleTieredMachine(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScalingFunction) {
        super(info, tier, false, tankScalingFunction);

        attachPersistentTrait("autoOutput", new AutoOutputTrait(List.of(exportItems), List.of(exportFluids)));
        attachPersistentTrait("circuit", new ProgrammableCircuitSlotTrait());
        attachPersistentTrait("batterySlot", new BatterySlotTrait(energyContainer));
    }

    public SimpleTieredMachine(BlockEntityCreationInfo info, int tier) {
        this(info, tier, GTMachineUtils.defaultTankSizeFunction);
    }

    @Override
    public long getDisplayRecipeVoltage() {
        return GTValues.V[this.tier];
    }
}
