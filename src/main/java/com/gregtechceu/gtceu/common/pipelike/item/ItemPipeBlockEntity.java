package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.blockentity.PipeBlockEntity;

import com.gregtechceu.gtceu.common.pipelike.GTPipeNetworks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ItemPipeBlockEntity extends PipeBlockEntity<ItemPipeVariant, ItemPipeSegmentProperties> {

    public ItemPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, GTPipeNetworks.ITEM);
    }

    public long getLevelTime() {
        return hasLevel() ? Objects.requireNonNull(getLevel()).getGameTime() : 0L;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return LazyOptional.empty();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public boolean canPipesConnect(Direction side, PipeBlockEntity<ItemPipeVariant, ItemPipeSegmentProperties> other) {
        return other instanceof ItemPipeBlockEntity;
    }

    @Override
    public boolean canPipeConnectToBlock(Direction side, Block block, @Nullable BlockEntity blockEntity) {
        return blockEntity != null &&
                blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side.getOpposite()).isPresent();
    }
}
