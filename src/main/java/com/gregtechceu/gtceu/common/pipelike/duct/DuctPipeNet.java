package com.gregtechceu.gtceu.common.pipelike.duct;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import java.util.*;

public class DuctPipeNet extends PipeNet<DuctPipeProperties> {

    private final Map<BlockPos, List<DuctRoutePath>> NET_DATA = new HashMap<>();

    public DuctPipeNet(LevelPipeNet<DuctPipeProperties, ? extends PipeNet<DuctPipeProperties>> world) {
        super(world);
    }

    public List<DuctRoutePath> getNetData(BlockPos pipePos, Direction facing) {
        List<DuctRoutePath> data = NET_DATA.get(pipePos);
        if (data == null) {
            data = DuctNetWalker.createNetData(this, pipePos, facing);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
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
    protected void transferNodeData(Map<BlockPos, Node<DuctPipeProperties>> transferredNodes,
                                    PipeNet<DuctPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((DuctPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(DuctPipeProperties nodeData, CompoundTag tagCompound) {
        tagCompound.putFloat("Rate", nodeData.getTransferRate());
    }

    @Override
    protected DuctPipeProperties readNodeData(CompoundTag tagCompound) {
        return new DuctPipeProperties(tagCompound.getFloat("Rate"));
    }
}
