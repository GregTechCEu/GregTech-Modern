package com.gregtechceu.gtceu.api.graphnet.servernet;

import com.gregtechceu.gtceu.api.graphnet.GraphNetBacker;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.alg.AlgorithmBuilder;
import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;
import com.gregtechceu.gtceu.api.graphnet.graph.INetGraph;
import com.gregtechceu.gtceu.api.graphnet.logic.WeightFactorLogic;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

/**
 * Unused demonstration net that would allow for edges bridging dimensions inside the graph representation.
 */
@SuppressWarnings("unused")
public abstract class ServerNet extends SavedData implements IGraphNet {

    protected final GraphNetBacker backer;

    /*
     * public static ServerNet get(String name, ServerLevel level, Function<IGraphNet, INetGraph> graphBuilder,
     * AlgorithmBuilder... algorithmBuilder) {
     * return level.getDataStorage().computeIfAbsent(tag -> new ServerNet(tag, graphBuilder, algorithmBuilder), () ->
     * new ServerNet(graphBuilder, algorithmBuilder), name);
     * }
     * 
     * public static ServerNet get(String name, ServerLevel level, boolean directed, AlgorithmBuilder...
     * algorithmBuilders) {
     * return get(name, level, directed ? NetDirectedGraph.standardBuilder() : NetUndirectedGraph.standardBuilder(),
     * algorithmBuilders);
     * }
     */

    public ServerNet(Function<IGraphNet, INetGraph> graphBuilder,
                     AlgorithmBuilder... algorithmBuilders) {
        this.backer = new GraphNetBacker(this, graphBuilder.apply(this), algorithmBuilders);
    }

    public ServerNet(CompoundTag tag, Function<IGraphNet, INetGraph> graphBuilder,
                     AlgorithmBuilder... algorithmBuilders) {
        this.backer = new GraphNetBacker(this, graphBuilder.apply(this), algorithmBuilders);
    }

    @Override
    public void addNode(@NotNull NetNode node) {
        nodeClassCheck(node);
        this.backer.addNode(node);
    }

    public @Nullable ServerNetNode getNode(@NotNull GlobalPos equivalencyData) {
        return (ServerNetNode) getNode((Object) equivalencyData);
    }

    @Override
    public @Nullable NetNode getNode(@NotNull Object equivalencyData) {
        return backer.getNode(equivalencyData);
    }

    @Override
    public void removeNode(@NotNull NetNode node) {
        nodeClassCheck(node);
        this.backer.removeNode(node);
    }

    @Override
    public NetEdge addEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays) {
        nodeClassCheck(source);
        nodeClassCheck(target);
        double weight = source.getData().getLogicEntryDefaultable(WeightFactorLogic.INSTANCE).getValue() +
                target.getData().getLogicEntryDefaultable(WeightFactorLogic.INSTANCE).getValue();
        NetEdge edge = backer.addEdge(source, target, weight);
        if (bothWays) {
            if (this.getGraph().isDirected()) {
                backer.addEdge(target, source, weight);
            }
            return null;
        } else return edge;
    }

    @Override
    public @Nullable NetEdge getEdge(@NotNull NetNode source, @NotNull NetNode target) {
        nodeClassCheck(source);
        nodeClassCheck(target);
        return backer.getEdge(source, target);
    }

    @Override
    public void removeEdge(@NotNull NetNode source, @NotNull NetNode target, boolean bothWays) {
        nodeClassCheck(source);
        nodeClassCheck(target);
        this.backer.removeEdge(source, target);
        if (bothWays && this.getGraph().isDirected()) {
            this.backer.removeEdge(target, source);
        }
    }

    public void load(@NotNull CompoundTag nbt) {
        backer.readFromNBT(nbt);
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
        return backer.writeToNBT(compound);
    }

    @Override
    public GraphNetBacker getBacker() {
        return backer;
    }

    @Override
    public Class<? extends NetNode> getNodeClass() {
        return ServerNetNode.class;
    }

    @Override
    public @NotNull ServerNetNode getNewNode() {
        return new ServerNetNode(this);
    }
}
