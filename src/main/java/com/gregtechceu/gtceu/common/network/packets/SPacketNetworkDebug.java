package com.gregtechceu.gtceu.common.network.packets;

import com.gregtechceu.gtceu.api.pipenet.LevelPipeNet;
import com.gregtechceu.gtceu.api.pipenet.Node;
import com.gregtechceu.gtceu.api.pipenet.PipeNet;
import com.gregtechceu.gtceu.client.renderer.NetworkDebugData;
import com.gregtechceu.gtceu.client.renderer.NetworkDebugRenderer;
import com.gregtechceu.gtceu.common.computation.ComputationNetworkManager;
import com.gregtechceu.gtceu.common.network.GTNetwork;
import com.gregtechceu.gtceu.common.pipelike.cable.LevelEnergyNet;
import com.gregtechceu.gtceu.common.pipelike.duct.LevelDuctPipeNet;
import com.gregtechceu.gtceu.common.pipelike.fluidpipe.LevelFluidPipeNet;
import com.gregtechceu.gtceu.common.pipelike.item.LevelItemPipeNet;
import com.gregtechceu.gtceu.common.pipelike.laser.LevelLaserPipeNet;
import com.gregtechceu.gtceu.common.pipelike.optical.LevelOpticalPipeNet;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SPacketNetworkDebug implements GTNetwork.INetPacket {

    private final List<DebugNetwork> networks;

    public SPacketNetworkDebug(ServerLevel level) {
        this.networks = new ArrayList<>();
        addPipeNetworks(NetworkType.ENERGY, LevelEnergyNet.getOrCreate(level));
        addPipeNetworks(NetworkType.FLUID, LevelFluidPipeNet.getOrCreate(level));
        addPipeNetworks(NetworkType.ITEM, LevelItemPipeNet.getOrCreate(level));
        addPipeNetworks(NetworkType.OPTICAL, LevelOpticalPipeNet.getOrCreate(level));
        addPipeNetworks(NetworkType.LASER, LevelLaserPipeNet.getOrCreate(level));
        addPipeNetworks(NetworkType.DUCT, LevelDuctPipeNet.getOrCreate(level));
        addComputationNetworks(ComputationNetworkManager.get(level));
    }

    public SPacketNetworkDebug(FriendlyByteBuf buf) {
        int networkCount = buf.readVarInt();
        this.networks = new ArrayList<>(networkCount);
        for (int i = 0; i < networkCount; i++) {
            NetworkType type = buf.readEnum(NetworkType.class);
            int nodeCount = buf.readVarInt();
            List<BlockPos> nodes = new ArrayList<>(nodeCount);
            for (int node = 0; node < nodeCount; node++) {
                nodes.add(buf.readBlockPos());
            }
            int edgeCount = buf.readVarInt();
            List<DebugEdge> edges = new ArrayList<>(edgeCount);
            for (int edge = 0; edge < edgeCount; edge++) {
                edges.add(new DebugEdge(buf.readBlockPos(), buf.readBlockPos()));
            }
            networks.add(new DebugNetwork(type, nodes, edges));
        }
    }

    @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(networks.size());
        for (DebugNetwork network : networks) {
            buf.writeEnum(network.type());
            buf.writeVarInt(network.nodes().size());
            for (BlockPos node : network.nodes()) {
                buf.writeBlockPos(node);
            }
            buf.writeVarInt(network.edges().size());
            for (DebugEdge edge : network.edges()) {
                buf.writeBlockPos(edge.first());
                buf.writeBlockPos(edge.second());
            }
        }
    }

    @Override
    public void execute(NetworkEvent.Context context) {
        NetworkDebugRenderer.setNetworks(networks.stream()
                .map(network -> new NetworkDebugData(NetworkDebugData.Type.valueOf(network.type().name()),
                        network.nodes(), network.edges().stream()
                                .map(edge -> new NetworkDebugData.Edge(edge.first(), edge.second()))
                                .toList()))
                .toList());
    }

    private void addPipeNetworks(NetworkType type, LevelPipeNet<?, ? extends PipeNet<?>> levelPipeNet) {
        for (PipeNet<?> pipeNet : levelPipeNet.getPipeNets()) {
            List<BlockPos> nodes = new ArrayList<>(pipeNet.getAllNodes().keySet());
            List<DebugEdge> edges = new ArrayList<>();
            collectPipeEdges(pipeNet, edges);
            networks.add(new DebugNetwork(type, nodes, edges));
        }
    }

    private void collectPipeEdges(PipeNet<?> pipeNet, List<DebugEdge> edges) {
        for (Map.Entry<BlockPos, ? extends Node<?>> entry : pipeNet.getAllNodes().entrySet()) {
            BlockPos pos = entry.getKey();
            for (Direction direction : GTUtil.DIRECTIONS) {
                BlockPos other = pos.relative(direction);
                if (pos.asLong() < other.asLong() && pipeNet.isNodeConnectedTo(pos, direction)) {
                    edges.add(new DebugEdge(pos, other));
                }
            }
        }
    }

    private void addComputationNetworks(ComputationNetworkManager manager) {
        for (ComputationNetworkManager.DebugTopology topology : manager.getDebugTopologies()) {
            List<DebugEdge> edges = topology.edges().stream()
                    .map(edge -> new DebugEdge(edge.first(), edge.second()))
                    .toList();
            networks.add(new DebugNetwork(NetworkType.COMPUTATION, topology.nodes(), edges));
        }
    }

    public record DebugNetwork(NetworkType type, List<BlockPos> nodes, List<DebugEdge> edges) {}

    public record DebugEdge(BlockPos first, BlockPos second) {}

    public enum NetworkType {

        ENERGY(0xFF3A3A),
        FLUID(0x34B9FF),
        ITEM(0xFFB13B),
        OPTICAL(0xD95CFF),
        LASER(0xFF4FFF),
        DUCT(0x61D65F),
        COMPUTATION(0xFFE45C);

        public final int color;

        NetworkType(int color) {
            this.color = color;
        }
    }
}
