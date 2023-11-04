package com.gregtechceu.gtceu.common.blockentity.fabric;

import com.gregtechceu.gtceu.api.capability.fabric.GTCapability;
import com.gregtechceu.gtceu.common.blockentity.ItemPipeBlockEntity;
import com.lowdragmc.lowdraglib.side.item.fabric.ItemTransferHelperImpl;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemPipeBlockEntityImpl extends ItemPipeBlockEntity {
    public ItemPipeBlockEntityImpl(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public static ItemPipeBlockEntity create(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new ItemPipeBlockEntityImpl(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<ItemPipeBlockEntity> type) {
        ItemStorage.SIDED.registerForBlockEntity((blockEntity, direction) -> {
            Level world = blockEntity.getLevel();
            if (world.isClientSide())
                return null;

            if (blockEntity.getHandlers().size() == 0)
                blockEntity.initHandlers();
            blockEntity.checkNetwork();
            return ItemTransferHelperImpl.toItemVariantStorage(blockEntity.getHandlers().getOrDefault(direction, blockEntity.getDefaultHandler()));
        }, type);
        GTCapability.CAPABILITY_COVERABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity.getCoverContainer(), type);
        GTCapability.CAPABILITY_TOOLABLE.registerForBlockEntity((blockEntity, direction) -> blockEntity, type);
    }
}
