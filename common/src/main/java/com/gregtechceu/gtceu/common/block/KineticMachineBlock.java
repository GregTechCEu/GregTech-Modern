package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.api.block.BlockProperties;
import com.gregtechceu.gtceu.api.block.MetaMachineBlock;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import com.gregtechceu.gtceu.common.machine.KineticMachineDefinition;
import com.simibubi.create.content.contraptions.base.IRotate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
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
        return getFrontFacing(state) == face;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return getFrontFacing(state).getAxis();
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (!level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pState.getValue(BlockProperties.SERVER_TICK) && pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                    if (pTile instanceof KineticMachineBlockEntity kineticMachineBlockEntity) {
                        kineticMachineBlockEntity.tick();
                    }
                };
            } else  {
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
