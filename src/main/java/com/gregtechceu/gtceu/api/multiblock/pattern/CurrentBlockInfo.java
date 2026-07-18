package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.GTCEu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CurrentBlockInfo {

    @Setter
    @Getter
    protected @Nullable Level level;
    @Getter
    private BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
    @Getter
    private @Nullable BlockState blockState;
    @Getter
    private @Nullable BlockEntity blockEntity;
    private boolean resolvedBlockEntity;

    public BlockState retrieveCurrentBlockState() {
        if (this.blockState == null && level != null) {
            this.blockState = level.getBlockState(pos);
        }
        Objects.requireNonNull(blockState, String.format("Failed to retrieve block state at %s", pos));
        return blockState;
    }

    public @Nullable BlockEntity retrieveCurrentBlockEntity() {
        BlockState state = retrieveCurrentBlockState();
        if (!state.hasBlockEntity()) {
            return null;
        }
        if (blockEntity == null && !resolvedBlockEntity && level != null) {
            blockEntity = level.getBlockEntity(pos);
            resolvedBlockEntity = true;
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
            GTCEu.LOGGER.error("Level is null in CurrentBlockInfo");
            return;
        }
        blockState = level.getBlockState(pos);
        blockEntity = level.getBlockEntity(pos);
        resolvedBlockEntity = true;
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
