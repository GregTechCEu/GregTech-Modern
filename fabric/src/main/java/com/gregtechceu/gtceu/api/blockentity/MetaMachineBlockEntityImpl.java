package com.gregtechceu.gtceu.api.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/2/17
 * @implNote MetaMachineBlockEntity
 */
public class MetaMachineBlockEntityImpl extends MetaMachineBlockEntity {

    public MetaMachineBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

}
