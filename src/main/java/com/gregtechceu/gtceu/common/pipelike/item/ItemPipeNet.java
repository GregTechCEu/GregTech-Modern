package com.gregtechceu.gtceu.common.pipelike.item;

import com.gregtechceu.gtceu.api.data.chemical.material.properties.ItemPipeProperties;
import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class ItemPipeNet extends PipeNet<ItemPipeProperties> {

    private final Map<BlockPos, List<ItemRoutePath>> NET_DATA = new HashMap<>();
    private final Map<BlockPos, List<ItemRoutePath>> NET_DATA_NO_RESTRICTIVE = new HashMap<>();
    private final Map<BlockPos, List<ItemRoutePath>> NET_DATA_ONLY_RESTRICTIVE = new HashMap<>();

    public ItemPipeNet(LevelPipeNet<ItemPipeProperties, ? extends PipeNet<ItemPipeProperties>> world) {
        super(world);
    }

    public List<ItemRoutePath> getNetData(BlockPos pipePos, Direction facing, ItemRoutePathSet ITEMNETSET) {
        List<ItemRoutePath> data = switch (ITEMNETSET) {
            case FULL -> NET_DATA.get(pipePos);
            case NONRESTRICTED -> NET_DATA_NO_RESTRICTIVE.get(pipePos);
            case RESTRICTED -> NET_DATA_ONLY_RESTRICTIVE.get(pipePos);
        };

        if (data == null) {
            data = ItemNetWalker.createNetData(this, pipePos, facing);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(inv -> inv.getProperties().getPriority()));

            // split between the three lists
            // Making the walker explicitly return only one of the lists would be too API-intrusive for a non-X.0.0
            // release
            List<ItemRoutePath> nonRestricted = new ArrayList<>(), restricted = new ArrayList<>();
            for (ItemRoutePath route : data) {
                if (route.isRestrictive()) {
                    restricted.add(route);
                } else {
                    nonRestricted.add(route);
                }
            }

            NET_DATA.put(pipePos, data);
            NET_DATA_NO_RESTRICTIVE.put(pipePos, nonRestricted);
            NET_DATA_ONLY_RESTRICTIVE.put(pipePos, restricted);

            data = switch (ITEMNETSET) {
                case FULL -> NET_DATA.get(pipePos);
                case NONRESTRICTED -> NET_DATA_NO_RESTRICTIVE.get(pipePos);
                case RESTRICTED -> NET_DATA_ONLY_RESTRICTIVE.get(pipePos);
            };
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        clearNetData();
    }

    private void clearNetData() {
        NET_DATA.clear();
        NET_DATA_ONLY_RESTRICTIVE.clear();
        NET_DATA_NO_RESTRICTIVE.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        clearNetData();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<ItemPipeProperties>> transferredNodes,
                                    PipeNet<ItemPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        clearNetData();
        ((ItemPipeNet) parentNet).clearNetData();
    }

    @Override
    protected void writeNodeData(ItemPipeProperties nodeData, CompoundTag tagCompound) {
        tagCompound.putInt("Resistance", nodeData.getPriority());
        tagCompound.putFloat("Rate", nodeData.getTransferRate());
    }

    @Override
    protected ItemPipeProperties readNodeData(CompoundTag tagCompound) {
        return new ItemPipeProperties(tagCompound.getInt("Resistance"), tagCompound.getFloat("Rate"));
    }
}
