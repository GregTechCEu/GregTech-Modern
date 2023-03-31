package com.gregtechceu.gtceu.common.blockentity.fabric;

import com.gregtechceu.gtceu.api.blockentity.fabric.MetaMachineBlockEntityImpl;
import com.gregtechceu.gtceu.common.blockentity.KineticMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * @author KilaBash
 * @date 2023/3/31
 * @implNote KineticMachineBlockEntityImpl
 */
public class KineticMachineBlockEntityImpl extends KineticMachineBlockEntity{

    protected KineticMachineBlockEntityImpl(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public static KineticMachineBlockEntity create(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        return new KineticMachineBlockEntityImpl(typeIn, pos, state);
    }

    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> blockEntityType) {
        MetaMachineBlockEntityImpl.onBlockEntityRegister(blockEntityType);
    }
}
