package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.pipenet.PipeBlockEntity;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardCleanerTrait;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardEmitterTrait;
import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class DuctPipeBlockEntity extends PipeBlockEntity<DuctPipeType> {

    public DuctPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, GTPipeNetworks.DUCT, pos, blockState);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    public boolean canAttachTo(Direction side) {
        if (level != null) {
            if (level.getBlockEntity(getBlockPos().relative(side)) instanceof DuctPipeBlockEntity) {
                return false;
            }
            BlockPos relative = getBlockPos().relative(side);
            return GTCapabilityHelper.getHazardContainer(level, relative, side.getOpposite()) != null ||
                    (level.getBlockEntity(relative) instanceof MetaMachine machine &&
                            (machine.getTrait(EnvironmentalHazardCleanerTrait.TYPE) != null ||
                                    machine.getTrait(EnvironmentalHazardEmitterTrait.TYPE) != null));
        }
        return false;
    }
}
