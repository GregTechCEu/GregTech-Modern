package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.block.property.GTBlockStateProperties;
import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;
import com.gregtechceu.gtceu.api.capability.GTCapability;
import com.gregtechceu.gtceu.common.pipelike.laser.LaserPipeBlockEntity;

import net.minecraft.MethodsReturnNonnullByDefault;
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
@MethodsReturnNonnullByDefault
public class OpticalPipeBlockEntity extends PipeBlockEntity<OpticalPipeVariant, OpticalPipeSegmentProperties> {

    public OpticalPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public boolean canHaveBlockedFaces() {
        return false;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GTCapability.CAPABILITY_DATA_ACCESS) {
            return LazyOptional.empty();
        }

        if (capability == GTCapability.CAPABILITY_COMPUTATION_PROVIDER) {
            return LazyOptional.empty();
        }

        return super.getCapability(capability, facing);
    }

    @Override
    public void setConnection(Direction side, boolean connected, boolean fromNeighbor) {
        if (!getLevel().isClientSide && connected && !fromNeighbor) {
            // never allow more than two connections total
            if (getNumConnections() >= 2) return;

            // also check the other pipe
            BlockEntity tile = getLevel().getBlockEntity(this.getBlockPos().relative(side));
            if (tile instanceof OpticalPipeBlockEntity other) {
                if (other.getNumConnections() >= 2) return;
            }
        }
        super.setConnection(side, connected, fromNeighbor);
    }

    /**
     * @param active   if the pipe should become active
     * @param duration how long the pipe should be active for
     */
    public void setActive(boolean active, int duration) {
        LaserPipeBlockEntity.setPipeActive(this, this.getBlockState(), active, duration);
    }

    public boolean isActive() {
        return this.getBlockState().getValue(GTBlockStateProperties.ACTIVE);
    }

    @Override
    public boolean canPipesConnect(Direction side, PipeBlockEntity<OpticalPipeVariant, OpticalPipeSegmentProperties> other) {
        return other instanceof OpticalPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(Direction side, Block block, @Nullable BlockEntity blockEntity) {
        if (blockEntity == null) return false;
        if (blockEntity.getCapability(GTCapability.CAPABILITY_DATA_ACCESS, side.getOpposite()).isPresent()) return true;
        return blockEntity.getCapability(GTCapability.CAPABILITY_COMPUTATION_PROVIDER, side.getOpposite()).isPresent();
    }
}
