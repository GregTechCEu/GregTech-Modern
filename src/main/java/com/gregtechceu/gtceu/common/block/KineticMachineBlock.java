package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;
import com.gregtechceu.gtceu.common.machine.kinetic.IKineticMachine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote KineticMachineBlock
 */
public class KineticMachineBlock extends MetaMachineBlock implements IRotate {

    public KineticMachineBlock(Properties properties, KineticMachineDefinition definition) {
        super(properties, definition);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        if (MetaMachine.getMachine(world, pos) instanceof IKineticMachine kineticMachine) {
            return kineticMachine.hasShaftTowards(face);
        }
        return false;
    }

    public Direction getRotationFacing(BlockState state) {
        var frontFacing = getFrontFacing(state);
        return ((KineticMachineDefinition) definition).isFrontRotation() ? frontFacing :
                (frontFacing.getAxis() == Direction.Axis.Y ? Direction.NORTH : frontFacing.getClockWise());
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return getRotationFacing(state).getAxis();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        // onBlockAdded is useless for init, as sometimes the TE gets re-instantiated

        // however, if a block change occurs that does not change kinetic connections,
        // we can prevent a major re-propagation here

        BlockEntity tileEntity = level.getBlockEntity(pos);
        if (tileEntity instanceof KineticBlockEntity kineticBlockEntity) {
            kineticBlockEntity.preventSpeedUpdate = 0;

            if (oldState.getBlock() != state.getBlock())
                return;
            if (state.hasBlockEntity() != oldState.hasBlockEntity())
                return;
            if (!areStatesKineticallyEquivalent(oldState, state))
                return;

            kineticBlockEntity.preventSpeedUpdate = 2;
        }
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(this.rotationState.property,
                pRotation.rotate(pState.getValue(this.rotationState.property)));
    }

    public boolean areStatesKineticallyEquivalent(BlockState oldState, BlockState newState) {
        if (oldState.getBlock() != newState.getBlock())
            return false;
        return getRotationAxis(newState) == getRotationAxis(oldState);
    }

    @Override
    public void updateIndirectNeighbourShapes(BlockState stateIn, LevelAccessor worldIn, BlockPos pos, int flags,
                                              int count) {
        if (worldIn.isClientSide())
            return;

        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (!(tileEntity instanceof KineticBlockEntity kte))
            return;

        if (kte.preventSpeedUpdate > 0)
            return;

        // Remove previous information when block is added
        kte.warnOfMovement();
        kte.clearKineticInformation();
        kte.updateSpeed = true;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                            BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (!level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pState.getValue(BlockProperties.SERVER_TICK) &&
                            pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                    if (pTile instanceof KineticMachineBlockEntity kineticMachineBlockEntity) {
                        kineticMachineBlockEntity.tick();
                    }
                };
            } else {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                    if (pTile instanceof KineticMachineBlockEntity kineticMachineBlockEntity) {
                        kineticMachineBlockEntity.tick();
                    }
                };
            }
        }
        return null;
    }
}
