package com.lowdragmc.gtceu.api.registry.registrate.forge;

import com.lowdragmc.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.lowdragmc.gtceu.blockentity.MetaMachineBlockEntityImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/2/19
 * @implNote MachineBuilderImpl
 */
public class MachineBuilderImpl {
    public static MetaMachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new MetaMachineBlockEntityImpl(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<MetaMachineBlockEntity> metaMachineBlockEntityBlockEntityType) {
    }
}
