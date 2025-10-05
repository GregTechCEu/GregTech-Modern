package com.gregtechceu.gtceu.utils.fakelevel;

import com.google.common.base.Preconditions;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * BlockInfo represents immutable information for block in world
 * This includes block state and block entity, and needed for complete representation
 * of some complex blocks like machines, when rendering or manipulating them without world instance
 */
public class BlockInfo {

    public static final BlockInfo EMPTY = new BlockInfo(Blocks.AIR);
    public static final BlockInfo INVALID = new BlockInfo(Blocks.AIR);

    public static BlockInfo of(BlockGetter level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.isAir()) {
            return EMPTY;
        }
        BlockEntity block = null;
        if (blockState.hasBlockEntity()) {
            block = level.getBlockEntity(pos);
        }
        return new BlockInfo(blockState, block);
    }

    @Getter
    private BlockState blockState;
    @Getter
    private BlockEntity blockEntity;

    public BlockInfo(@NotNull Block block) {
        this(block.defaultBlockState());
    }

    public BlockInfo(@NotNull BlockState blockState) {
        this(blockState, null);
    }

    public BlockInfo(@NotNull BlockState blockState, @Nullable BlockEntity blockEntity) {
        set(blockState, blockEntity);
    }

    public void apply(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, blockState);
        if (blockEntity != null) {
            level.setBlockEntity(blockEntity);
        } else {
            blockEntity = level.getBlockEntity(pos);
        }
    }

    BlockInfo set(BlockState state, BlockEntity block) {
        Preconditions.checkNotNull(state, "Block state must not be null!");
        Preconditions.checkArgument(block == null || state.hasBlockEntity(),
                "Cannot create block info with block entity for block not having it!");
        this.blockState = state;
        this.blockEntity = block;
        return this;
    }

    public boolean isMutable() {
        return false;
    }

    public Mutable toMutable() {
        return new Mutable(this.blockState, this.blockEntity);
    }

    public BlockInfo toImmutable() {
        return this;
    }

    public BlockInfo copy() {
        return new BlockInfo(this.blockState, this.blockEntity);
    }

    public static class Mutable extends BlockInfo {

        public static final Mutable SHARED = new Mutable();

        public Mutable() {
            this(Blocks.AIR);
        }

        public Mutable(@NotNull Block block) {
            super(block);
        }

        public Mutable(@NotNull BlockState blockState) {
            super(blockState);
        }

        public Mutable(@NotNull BlockState blockState, @Nullable BlockEntity blockEntity) {
            super(blockState, blockEntity);
        }

        @Override
        public Mutable set(BlockState state, BlockEntity block) {
            return (Mutable) super.set(state, block);
        }

        public Mutable set(BlockGetter world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            BlockEntity block = null;
            if (blockState.hasBlockEntity()) {
                block = world.getBlockEntity(pos);
            }
            return set(blockState, block);
        }

        @Override
        public boolean isMutable() {
            return true;
        }

        @Override
        public Mutable toMutable() {
            return this;
        }

        @Override
        public BlockInfo toImmutable() {
            return new BlockInfo(getBlockState(), getBlockEntity());
        }

        @Override
        public Mutable copy() {
            return new Mutable(getBlockState(), getBlockEntity());
        }
    }
}
