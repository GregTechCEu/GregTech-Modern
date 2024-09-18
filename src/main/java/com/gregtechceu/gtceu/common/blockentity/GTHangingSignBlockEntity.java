package com.gregtechceu.gtceu.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GTHangingSignBlockEntity extends SignBlockEntity {

    private static final int MAX_TEXT_LINE_WIDTH = 60;
    private static final int TEXT_LINE_HEIGHT = 9;

    public GTHangingSignBlockEntity(BlockEntityType<? extends GTHangingSignBlockEntity> type, BlockPos pos,
                                    BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public int getTextLineHeight() {
        return TEXT_LINE_HEIGHT;
    }

    @Override
    public int getMaxTextLineWidth() {
        return MAX_TEXT_LINE_WIDTH;
    }
}
