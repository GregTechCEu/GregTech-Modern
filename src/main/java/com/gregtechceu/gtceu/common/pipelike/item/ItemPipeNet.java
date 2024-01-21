package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;
import com.gregtechceu.gtceu.utils.FacingPos;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.ItemTransferHelper;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;
import java.util.function.Predicate;

public class ItemPipeNet extends PipeNet<ItemPipeProperties> {

    private final Map<BlockPos, List<Inventory>> NET_DATA = new HashMap<>();

    public ItemPipeNet(LevelPipeNet<ItemPipeProperties, ? extends PipeNet<ItemPipeProperties>> world) {
        super(world);
    }

    public List<Inventory> getNetData(BlockPos pipePos) {
        List<Inventory> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = ItemNetWalker.createNetData(this, pipePos);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(inv -> inv.properties.getPriority()));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<ItemPipeProperties>> transferredNodes, PipeNet<ItemPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((ItemPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(ItemPipeProperties nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("Resistance", nodeData.getPriority());
        tagCompound.putFloat("Rate", nodeData.getTransferRate());
    }

    @Override
    protected ItemPipeProperties readNodeData(CompoundTag tagCompound) {
        return new ItemPipeProperties(tagCompound.getInt("Range"), tagCompound.getFloat("Rate"));
    }

    //////////////////////////////////////
    //*******     Pipe Status    *******//
    //////////////////////////////////////

    public static class Inventory {
        @Getter
        private final BlockPos pipePos;
        @Getter
        private final Direction faceToHandler;
        @Getter
        private final int distance;
        @Getter
        private final ItemPipeProperties properties;
        @Getter
        private final List<Predicate<ItemStack>> filters;

        public Inventory(BlockPos pipePos, Direction facing, int distance, ItemPipeProperties properties, List<Predicate<ItemStack>> filters) {
            this.pipePos = pipePos;
            this.faceToHandler = facing;
            this.distance = distance;
            this.properties = properties;
            this.filters = filters;
        }

        public boolean matchesFilters(ItemStack stack) {
            for (Predicate<ItemStack> filter : filters) {
                if (!filter.test(stack)) {
                    return false;
                }
            }
            return true;
        }

        public BlockPos getHandlerPos() {
            return pipePos.relative(faceToHandler);
        }

        public IItemTransfer getHandler(Level world) {
            BlockEntity tile = world.getBlockEntity(getHandlerPos());
            if (tile != null)
                return ItemTransferHelper.getItemTransfer(world, getHandlerPos(), faceToHandler.getOpposite());
            return null;
        }

        public FacingPos toFacingPos() {
            return new FacingPos(this.getPipePos(), this.faceToHandler);
        }
    }
}