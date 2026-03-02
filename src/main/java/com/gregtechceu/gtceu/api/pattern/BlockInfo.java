package com.gregtechceu.gtceu.api.pattern;

import com.gregtechceu.gtceu.client.util.FacadeBlockAndTintGetter;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Getter;
import lombok.Setter;

import java.util.function.Consumer;

@Setter
public class BlockInfo {

    public static final BlockInfo EMPTY;
    @Getter
    private BlockState blockState;
    private boolean hasBlockEntity;
    private CompoundTag tag;
    private ItemStack itemStack;
    private Consumer<BlockEntity> postCreate;
    private BlockEntity lastEntity;

    public BlockInfo(Block block) {
        this(block.defaultBlockState());
    }

    public BlockInfo(BlockState blockState) {
        this(blockState, false);
    }

    public BlockInfo(BlockState blockState, boolean hasBlockEntity) {
        this(blockState, hasBlockEntity, (ItemStack) null, null);
    }

    public BlockInfo(BlockState blockState, Consumer<BlockEntity> postCreate) {
        this(blockState, true, (ItemStack) null, postCreate);
    }

    public BlockInfo(BlockState blockState, boolean hasBlockEntity, ItemStack itemStack,
                     Consumer<BlockEntity> postCreate) {
        this.blockState = blockState;
        this.hasBlockEntity = hasBlockEntity;
        this.itemStack = itemStack;
        this.postCreate = postCreate;
    }

    public static BlockInfo fromBlockState(BlockState state) {
        try {
            if (state.getBlock() instanceof EntityBlock) {
                BlockEntity blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(BlockPos.ZERO, state);
                if (blockEntity != null) {
                    return new BlockInfo(state, true);
                }
            }
        } catch (Exception var2) {}

        return new BlockInfo(state);
    }

    public static BlockInfo fromBlock(Block block) {
        return fromBlockState(block.defaultBlockState());
    }

    public boolean hasBlockEntity() {
        return this.hasBlockEntity;
    }

    public BlockEntity getBlockEntity(BlockPos pos) {
        if (this.hasBlockEntity) {
            Block var3 = this.blockState.getBlock();
            if (var3 instanceof EntityBlock) {
                EntityBlock entityBlock = (EntityBlock) var3;
                if (this.lastEntity != null && this.lastEntity.getBlockPos().equals(pos)) {
                    return this.lastEntity;
                }

                this.lastEntity = entityBlock.newBlockEntity(pos, this.blockState);
                if (this.tag != null && this.lastEntity != null) {
                    CompoundTag compoundTag2 = this.lastEntity.saveWithoutMetadata();
                    CompoundTag compoundTag3 = compoundTag2.copy();
                    compoundTag2.merge(this.tag);
                    if (!compoundTag2.equals(compoundTag3)) {
                        this.lastEntity.load(compoundTag2);
                    }
                }

                if (this.postCreate != null) {
                    this.postCreate.accept(this.lastEntity);
                }

                return this.lastEntity;
            }
        }

        return null;
    }

    public BlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity entity = this.getBlockEntity(pos);
        if (entity != null) {
            entity.setLevel(level);
        }

        return entity;
    }

    public ItemStack getItemStackForm() {
        return this.itemStack == null ? new ItemStack(this.blockState.getBlock()) : this.itemStack;
    }

    public ItemStack getItemStackForm(BlockAndTintGetter level, BlockPos pos) {
        return this.itemStack != null ? this.itemStack : this.blockState.getBlock().getCloneItemStack(
                new FacadeBlockAndTintGetter(level, pos, this.blockState, null), pos, this.blockState);
    }

    public void apply(Level world, BlockPos pos) {
        world.setBlockAndUpdate(pos, this.blockState);
        BlockEntity blockEntity = this.getBlockEntity(pos);
        if (blockEntity != null) {
            world.setBlockEntity(blockEntity);
        }
    }

    public void clearBlockEntityCache() {
        this.lastEntity = null;
    }

    public BlockInfo() {}

    static {
        EMPTY = new BlockInfo(Blocks.AIR);
    }
}
