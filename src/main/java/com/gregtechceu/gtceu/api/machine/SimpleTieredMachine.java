package com.gregtechceu.gtceu.api.machine;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.trait.ProgrammableCircuitSlotTrait;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.api.sync_system.annotations.SyncToClient;
import com.gregtechceu.gtceu.common.machine.trait.AutoOutputTrait;
import com.gregtechceu.gtceu.common.machine.trait.BatterySlotTrait;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Getter;

import java.util.*;

/**
 * All simple single machines are implemented here.
 */
public class SimpleTieredMachine extends WorkableTieredMachine {

    @SaveField
    @SyncToClient
    public final AutoOutputTrait autoOutput;

    @Getter
    @SaveField
    protected final ProgrammableCircuitSlotTrait circuitSlot;

    @Getter
    @SaveField
    protected final BatterySlotTrait batterySlot;

    public SimpleTieredMachine(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScalingFunction) {
        super(info, tier, tankScalingFunction);

        this.autoOutput = attachTrait(new AutoOutputTrait(List.of(exportItems), List.of(exportFluids)));
        this.circuitSlot = attachTrait(new ProgrammableCircuitSlotTrait());
        this.batterySlot = attachTrait(new BatterySlotTrait(energyContainer));
    }

    /////////////////////////////////////
    // ****** RECIPE LOGIC *******//
    /////////////////////////////////////

    @Override
    public long getDisplayRecipeVoltage() {
        return GTValues.V[this.tier];
    }
}
