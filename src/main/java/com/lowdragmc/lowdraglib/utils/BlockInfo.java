package com.lowdragmc.lowdraglib.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR.defaultBlockState());

    private BlockState blockState;
    private boolean hasBlockEntity;
    private CompoundTag tag;
    private ItemStack itemStack;
    private Consumer<BlockEntity> postCreate;

    public BlockInfo() {
        this(Blocks.AIR.defaultBlockState());
    }

    public BlockInfo(Block block) {
        this(block.defaultBlockState());
    }

    public BlockInfo(BlockState blockState) {
        this(blockState, blockState.hasBlockEntity());
    }

    public BlockInfo(BlockState blockState, boolean hasBlockEntity) {
        this(blockState, hasBlockEntity, ItemStack.EMPTY, null);
    }

    public BlockInfo(BlockState blockState, Consumer<BlockEntity> postCreate) {
        this(blockState, blockState.hasBlockEntity(), ItemStack.EMPTY, postCreate);
    }

    public BlockInfo(BlockState blockState, boolean hasBlockEntity, ItemStack itemStack,
                     Consumer<BlockEntity> postCreate) {
        this.blockState = blockState;
        this.hasBlockEntity = hasBlockEntity;
        this.itemStack = itemStack;
        this.postCreate = postCreate;
    }

    public static BlockInfo fromBlockState(BlockState blockState) {
        return new BlockInfo(blockState);
    }

    public static BlockInfo fromBlock(Block block) {
        return new BlockInfo(block);
    }

    public BlockState getBlockState() {
        return blockState;
    }

    public boolean hasBlockEntity() {
        return hasBlockEntity;
    }

    public BlockEntity getBlockEntity(BlockPos pos, HolderLookup.Provider provider) {
        return blockState.hasBlockEntity() ? BlockEntity.loadStatic(pos, blockState, tag, provider) : null;
    }

    public BlockEntity getBlockEntity(HolderLookup.Provider provider, Level level, BlockPos pos) {
        return level.getBlockEntity(pos);
    }

    public ItemStack getItemStackForm() {
        if (itemStack != null && !itemStack.isEmpty()) {
            return itemStack.copy();
        }
        return new ItemStack(blockState.getBlock());
    }

    public ItemStack getItemStackForm(LevelReader level, BlockPos pos) {
        return getItemStackForm();
    }

    public void apply(HolderLookup.Provider provider, Level level, BlockPos pos) {
        level.setBlock(pos, blockState, Block.UPDATE_ALL);
        if (postCreate != null) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity != null) {
                postCreate.accept(blockEntity);
            }
        }
    }

    public void clearBlockEntityCache() {}

    public com.lowdragmc.lowdraglib2.utils.data.BlockInfo toLDLib2() {
        var info = new com.lowdragmc.lowdraglib2.utils.data.BlockInfo(blockState, hasBlockEntity, itemStack,
                postCreate);
        info.setTag(tag);
        return info;
    }

    public void setBlockState(BlockState blockState) {
        this.blockState = blockState;
    }

    public void setHasBlockEntity(boolean hasBlockEntity) {
        this.hasBlockEntity = hasBlockEntity;
    }

    public void setTag(CompoundTag tag) {
        this.tag = tag;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public void setPostCreate(Consumer<BlockEntity> postCreate) {
        this.postCreate = postCreate;
    }
}
