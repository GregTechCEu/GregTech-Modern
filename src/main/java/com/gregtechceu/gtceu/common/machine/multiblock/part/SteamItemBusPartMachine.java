package com.gregtechceu.gtceu.common.machine.multiblock.part;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.common.data.GTMachines;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class SteamItemBusPartMachine extends ItemBusPartMachine {

    private final String autoTooltipKey;

    public SteamItemBusPartMachine(BlockEntityCreationInfo info, IO io) {
        super(info, 1, io);
        autoTooltipKey = io == IO.IN ? "gtceu.gui.item_auto_input.tooltip" : "gtceu.gui.item_auto_output.tooltip";
    }

    @Override
    public boolean swapIO() {
        BlockPos blockPos = getBlockPos();
        MachineDefinition newDefinition = null;
        if (io == IO.IN) {
            newDefinition = GTMachines.STEAM_EXPORT_BUS;
        } else if (io == IO.OUT) {
            newDefinition = GTMachines.STEAM_IMPORT_BUS;
        }

        if (newDefinition == null) return false;
        BlockState newBlockState = newDefinition.getBlock().defaultBlockState();

        getLevel().setBlockAndUpdate(blockPos, newBlockState);

        if (getLevel().getBlockEntity(blockPos) instanceof SteamItemBusPartMachine newMachine) {
            // We don't set the circuit or distinct busses, since
            // that doesn't make sense on an output bus.
            // Furthermore, existing inventory items
            // and conveyors will drop to the floor on block override.
            newMachine.setFrontFacing(this.getFrontFacing());
            newMachine.setUpwardsFacing(this.getUpwardsFacing());
        }
        return true;
    }

    @Override
    public boolean isCircuitSlotEnabled() {
        return false;
    }
}
