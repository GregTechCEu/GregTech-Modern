package com.gregtechceu.gtceu.common.pipelike.optical;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;
import com.gregtechceu.gtceu.api.capability.IDataAccessMachine;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.computation.ComputationPort;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

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
        notifyCachedRouteEndpoints();
        notifyEndpointsAround(fromPos);
        NET_DATA.clear();
    }

    @Override
    protected void addNode(BlockPos nodePos, Node<OpticalPipeProperties> node) {
        notifyCachedRouteEndpoints();
        super.addNode(nodePos, node);
        notifyEndpointsAround(nodePos);
        notifyAllEndpointsInNet();
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        notifyCachedRouteEndpoints();
        NET_DATA.clear();
    }

    @Override
    public void updateBlockedConnections(BlockPos nodePos, Direction facing, boolean isBlocked) {
        notifyEndpointAt(nodePos.relative(facing), facing.getOpposite());
        super.updateBlockedConnections(nodePos, facing, isBlocked);
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<OpticalPipeProperties>> transferredNodes,
                                    PipeNet<OpticalPipeProperties> parentNet) {
        notifyCachedRouteEndpoints();
        ((OpticalPipeNet) parentNet).notifyCachedRouteEndpoints();
        super.transferNodeData(transferredNodes, parentNet);
        notifyAllEndpointsInNet();
        ((OpticalPipeNet) parentNet).notifyAllEndpointsInNet();
        NET_DATA.clear();
        ((OpticalPipeNet) parentNet).NET_DATA.clear();
    }

    private void notifyCachedRouteEndpoints() {
        for (Map.Entry<BlockPos, OpticalRoutePath> entry : NET_DATA.entrySet()) {
            notifyEndpointsAround(entry.getKey());
            notifyRouteTarget(entry.getValue());
        }
    }

    private void notifyAllEndpointsInNet() {
        for (BlockPos pipePos : getAllNodes().keySet()) {
            notifyEndpointsAround(pipePos);
        }
    }

    private void notifyRouteTarget(@Nullable OpticalRoutePath routePath) {
        if (routePath == null) return;
        notifyEndpointAt(routePath.getTargetPipePos().relative(routePath.getTargetFacing()),
                routePath.getTargetFacing().getOpposite());
    }

    private void notifyEndpointsAround(BlockPos pos) {
        for (Direction direction : GTUtil.DIRECTIONS) {
            notifyEndpointAt(pos.relative(direction), direction.getOpposite());
        }
    }

    private void notifyEndpointAt(BlockPos pos, @Nullable Direction side) {
        BlockEntity blockEntity = getLevel().getBlockEntity(pos);
        if (blockEntity == null) return;
        ComputationPort computationPort = blockEntity.getCapability(GTCapability.CAPABILITY_COMPUTATION_PORT, side)
                .resolve().orElse(null);
        if (computationPort != null && computationPort.getComputationPortPolicy().acceptsOptical()) {
            computationPort.onOpticalRouteChanged();
        }

        IDataAccessMachine dataAccess = blockEntity.getCapability(GTCapability.CAPABILITY_DATA_ACCESS, side)
                .resolve().orElse(null);
        if (dataAccess != null) {
            dataAccess.notifyListeners();
        }
    }

    @Override
    protected void writeNodeData(OpticalPipeProperties nodeData, CompoundTag tagCompound) {}

    @Override
    protected OpticalPipeProperties readNodeData(CompoundTag tagCompound) {
        return OpticalPipeProperties.INSTANCE;
    }
}
