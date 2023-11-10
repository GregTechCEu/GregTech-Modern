package com.gregtechceu.gtceu.common.blockentity.fabric;

import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.common.blockentity.LaserPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LaserPipeBlockEntityImpl extends LaserPipeBlockEntity {
    protected LaserPipeBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static LaserPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new LaserPipeBlockEntityImpl(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<LaserPipeBlockEntity> type) {
        GTCapability.CAPABILITY_LASER.registerForBlockEntity((blockEntity, direction) -> {
            Level world = blockEntity.getLevel();
            if (world.isClientSide())
                return blockEntity.clientCapability;

            if (blockEntity.getHandlers().isEmpty()) {
                blockEntity.initHandlers();
            }
            blockEntity.checkNetwork();
            return blockEntity.getHandlers().getOrDefault(direction, blockEntity.getDefaultHandler());
        }, type);
        GTCapability.CAPABILITY_COVERABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity.getCoverContainer(), type);
        GTCapability.CAPABILITY_TOOLABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity, type);
    }
}
