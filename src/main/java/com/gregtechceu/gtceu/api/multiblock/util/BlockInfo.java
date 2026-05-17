package com.gregtechceu.gtceu.api.multiblock.util;

import com.gregtechceu.gtceu.client.util.FakeBlockTintGetter;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;

public class BlockInfo {

    public static final FakeBlockTintGetter FAKE_LEVEL = new FakeBlockTintGetter();

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);

    @Getter
    private final BlockState blockState;
    private final boolean hasBlockEntity;
    private final ItemStack itemStack;
    @Getter
    private final BlockEntity blockEntity;

    public BlockInfo(Block block) {
        this(block.defaultBlockState());
    }

    public BlockInfo(BlockState blockState) {
        this(blockState, false);
    }

    public BlockInfo(BlockState blockState, boolean hasBlockEntity) {
        this(blockState, hasBlockEntity, null, null);
    }

    public BlockInfo(BlockState blockState, BlockEntity blockEntity) {
        this(blockState, true, null, blockEntity);
    }

    public BlockInfo(BlockState blockState, boolean hasBlockEntity, ItemStack itemStack, BlockEntity blockEntity) {
        this.blockState = blockState;
        this.hasBlockEntity = hasBlockEntity;
        this.itemStack = itemStack;
        this.blockEntity = blockEntity;

        FAKE_LEVEL.setState(blockState);
    }

    public static BlockInfo fromBlockState(BlockState state) {
        return new BlockInfo(state, state.hasBlockEntity());
    }

    public static BlockInfo fromBlock(Block block) {
        return fromBlockState(block.defaultBlockState());
    }

    public boolean hasBlockEntity() {
        return hasBlockEntity;
    }

    public BlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity entity = getBlockEntity();
        if (entity != null) {
            entity.setLevel(level);
        }
        return entity;
    }

    public ItemStack getItemStackForm() {
        return itemStack == null ? new ItemStack(blockState.getBlock()) : itemStack;
    }

    public ItemStack getItemStackForm(BlockAndTintGetter level, BlockPos pos) {
        if (itemStack != null) return itemStack;
        FAKE_LEVEL.setParent(level);
        FAKE_LEVEL.setPos(pos);
        return blockState.getBlock().getCloneItemStack(FAKE_LEVEL, pos, blockState);
    }

    public void apply(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, blockState);
        if (blockEntity != null) {
            level.setBlockEntity(blockEntity);
        }
    }
}
