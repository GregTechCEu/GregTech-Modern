package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.Setter;

public class CurrentBlockInfo {

    @Setter
    @Getter
    protected Level level;
    @Getter
    private BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    @Getter
    private BlockState blockState;
    @Getter
    private BlockEntity blockEntity;
    private boolean teResolved;

    public BlockState retrieveCurrentBlockState() {
        if (this.blockState == null && level != null) {
            this.blockState = level.getBlockState(pos);
        }
        return blockState;
    }

    public BlockEntity retrieveCurrentBlockEntity() {
        if (!retrieveCurrentBlockState().hasBlockEntity()) {
            return null;
        }
        if (blockEntity == null && !teResolved && level != null) {
            blockEntity = level.getBlockEntity(pos);
            teResolved = true;
        }
        return blockEntity;
    }

    public void setCurrentPos(BlockPos pos) {
        this.pos.set(pos);
        updateStateAndEntity();
    }

    public void setCurrentPos(BlockPos.MutableBlockPos pos) {
        this.pos.set(pos);
        updateStateAndEntity();
    }

    public BlockPos getBlockPos() {
        return pos;
    }

    private void updateStateAndEntity() {
        if (level == null) {
            GTCEu.LOGGER.error("CBI Level is null");
            return;
        }
        blockState = level.getBlockState(pos);
        blockEntity = level.getBlockEntity(pos);
        teResolved = true;
    }

    public CurrentBlockInfo shallowCopy() {
        CurrentBlockInfo ret = new CurrentBlockInfo();
        ret.level = level;
        ret.pos = pos;
        ret.blockState = blockState;
        ret.blockEntity = blockEntity;
        return ret;
    }
}
