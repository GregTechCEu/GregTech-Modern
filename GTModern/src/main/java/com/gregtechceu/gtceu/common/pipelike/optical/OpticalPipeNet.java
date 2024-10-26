package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class OpticalPipeNet extends PipeNet<OpticalPipeProperties> {

    private final Map<BlockPos, OpticalRoutePath> NET_DATA = new Object2ObjectOpenHashMap<>();

    public OpticalPipeNet(LevelPipeNet<OpticalPipeProperties, ? extends PipeNet<OpticalPipeProperties>> world) {
        super(world);
    }

    @Nullable
    public OpticalRoutePath getNetData(BlockPos pipePos, Direction facing) {
        if (NET_DATA.containsKey(pipePos)) {
            return NET_DATA.get(pipePos);
        }
        OpticalRoutePath data = OpticalNetWalker.createNetData(this, pipePos, facing);
        if (data == OpticalNetWalker.FAILED_MARKER) {
            // walker failed, don't cache, so it tries again on next insertion
            return null;
        }

        NET_DATA.put(pipePos, data);
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
    protected void transferNodeData(Map<BlockPos, Node<OpticalPipeProperties>> transferredNodes,
                                    PipeNet<OpticalPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((OpticalPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(OpticalPipeProperties nodeData, CompoundTag tagCompound) {}

    @Override
    protected OpticalPipeProperties readNodeData(CompoundTag tagCompound) {
        return OpticalPipeProperties.INSTANCE;
    }
}
