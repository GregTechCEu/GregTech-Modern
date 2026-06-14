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

import com.mojang.serialization.Codec;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class BlockInfo {

    public static final Codec<BlockInfo> CODEC = BlockState.CODEC.xmap(BlockInfo::fromBlockState,
            BlockInfo::getBlockState);

    public static final FakeBlockTintGetter FAKE_LEVEL = new FakeBlockTintGetter();

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);

    @Getter
    private final BlockState blockState;
    private final @Nullable ItemStack itemStack;
    @Getter
    private final @Nullable BlockEntity blockEntity;

    public BlockInfo(Block block) {
        this(block.defaultBlockState());
    }

    public BlockInfo(BlockState blockState) {
        this(blockState, null, null);
    }

    public BlockInfo(BlockState blockState, @Nullable BlockEntity blockEntity) {
        this(blockState, null, blockEntity);
    }

    public BlockInfo(BlockState blockState, @Nullable ItemStack itemStack,
                     @Nullable BlockEntity blockEntity) {
        this.blockState = blockState;
        this.itemStack = itemStack;
        this.blockEntity = blockEntity;

        FAKE_LEVEL.setState(blockState);
    }

    public static BlockInfo fromBlockState(BlockState state) {
        return new BlockInfo(state);
    }

    public static BlockInfo fromBlock(Block block) {
        return fromBlockState(block.defaultBlockState());
    }

    public @Nullable BlockEntity getBlockEntity(Level level, BlockPos pos) {
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

        BlockAndTintGetter oldParent = FAKE_LEVEL.parent;
        try {
            FAKE_LEVEL.setParent(level);
            FAKE_LEVEL.setState(this.blockState);
            FAKE_LEVEL.setPos(pos);
            return blockState.getBlock().getCloneItemStack(FAKE_LEVEL, pos, this.blockState);
        } finally {
            FAKE_LEVEL.setParent(oldParent);
        }
    }

    public void apply(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, blockState);
        if (blockEntity != null) {
            level.setBlockEntity(blockEntity);
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlockInfo blockInfo = (BlockInfo) o;
        return Objects.equals(this.blockState, blockInfo.blockState);
    }

    @Override
    public int hashCode() {
        return this.blockState.hashCode();
    }
}
