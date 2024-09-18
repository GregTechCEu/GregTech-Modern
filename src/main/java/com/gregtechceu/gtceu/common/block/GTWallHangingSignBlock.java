package com.gregtechceu.gtceu.common.block;

import com.gregtechceu.gtceu.common.blockentity.GTHangingSignBlockEntity;
import com.gregtechceu.gtceu.common.data.GTBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import javax.annotation.Nullable;

public class GTWallHangingSignBlock extends WallHangingSignBlock {

    public GTWallHangingSignBlock(Properties properties, WoodType type) {
        super(properties, type);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GTHangingSignBlockEntity(GTBlockEntities.GT_HANGING_SIGN.get(), pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, GTBlockEntities.GT_HANGING_SIGN.get(), SignBlockEntity::tick);
    }
}
