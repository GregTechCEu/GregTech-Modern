package com.gregtechceu.gtceu.api.sync_system.managed;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

/**
 * Entity block that implements the default ticker for a {@link ManagedSyncBlockEntity}
 */
public interface ManagedSyncEntityBlock extends EntityBlock {

    @Override
    @Nullable
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> blockEntityType) {
        if (!level.isClientSide) {
            return (pLevel, pPos, pState, pTile) -> {
                if (pTile instanceof ManagedSyncBlockEntity be) {
                    be.serverTick();
                }
            };
        } else {
            return (pLevel, pPos, pState, pTile) -> {
                if (pTile instanceof ManagedSyncBlockEntity be) {
                    be.clientTick();
                }
            };
        }
    }
}
