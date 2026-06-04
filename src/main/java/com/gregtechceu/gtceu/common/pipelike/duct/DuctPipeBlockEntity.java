package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardCleanerTrait;
import com.gregtechceu.gtceu.common.machine.trait.hazard.EnvironmentalHazardEmitterTrait;

import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class DuctPipeBlockEntity extends PipeBlockEntity<DuctSegmentProperties> {

    public DuctPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, GTPipeNetworks.DUCT);
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == GTCapability.CAPABILITY_HAZARD_CONTAINER) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    @Override
    public boolean canPipesConnect(Direction side, PipeBlockEntity<DuctSegmentProperties> other) {
        return other instanceof DuctPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(Direction side, Block block, @Nullable BlockEntity blockEntity) {
        return blockEntity != null &&
                (blockEntity.getCapability(GTCapability.CAPABILITY_HAZARD_CONTAINER, side.getOpposite()).isPresent() ||
                        blockEntity instanceof MetaMachine metaMachine &&
                                (metaMachine.getTrait(EnvironmentalHazardCleanerTrait.TYPE) != null ||
                                        metaMachine.getTrait(EnvironmentalHazardEmitterTrait.TYPE) !=
                                                null));
    }
}
