package com.gregtechceu.gtceu.common.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class GTHangingSignBlockEntity extends HangingSignBlockEntity {
    private static final int MAX_TEXT_LINE_WIDTH = 60;
    private static final int TEXT_LINE_HEIGHT = 9;

    
    public GTHangingSignBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(blockPos, blockState);
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
