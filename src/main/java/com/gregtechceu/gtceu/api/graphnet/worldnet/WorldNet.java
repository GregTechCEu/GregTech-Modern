package com.gregtechceu.gtceu.api.graphnet.worldnet;

import com.gregtechceu.gtceu.api.graphnet.GraphNetBacker;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.alg.AlgorithmBuilder;
import com.gregtechceu.gtceu.api.graphnet.edge.NetEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.graph.INetGraph;
import com.gregtechceu.gtceu.api.graphnet.graph.NetDirectedGraph;
import com.gregtechceu.gtceu.api.graphnet.graph.NetUndirectedGraph;
import com.gregtechceu.gtceu.api.graphnet.logic.WeightFactorLogic;
import com.gregtechceu.gtceu.api.graphnet.path.GenericGraphNetPath;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

public abstract class WorldNet extends SavedData implements IGraphNet, GenericGraphNetPath.Provider {

    protected final GraphNetBacker backer;
    @Getter
    @Setter
    private Level level;
    private ResourceKey<Level> fallbackDimension;

    public WorldNet(@NotNull Function<IGraphNet, INetGraph> graphBuilder,
                    AlgorithmBuilder... algorithmBuilders) {
        this.backer = new GraphNetBacker(this, graphBuilder.apply(this), algorithmBuilders);
    }

    public WorldNet(boolean directed, AlgorithmBuilder... algorithmBuilders) {
        this(directed ? NetDirectedGraph.standardBuilder() : NetUndirectedGraph.standardBuilder(),
                algorithmBuilders);
    }

    @Override
    public Iterator<GenericGraphNetPath> getPaths(NetNode node, IPredicateTestObject testObject,
                                                  @Nullable SimulatorKey simulator, long queryTick) {
        nodeClassCheck(node);
        return backer.getPaths(node, 0, GenericGraphNetPath.MAPPER, testObject, simulator, queryTick);
    }

    @NotNull
    public WorldNetNode getOrCreateNode(@NotNull BlockPos pos) {
        WorldNetNode node = getNode(pos);
        if (node != null) return node;
        node = getNewNode();
        node.setPos(pos);
        addNode(node);
        return node;
    }

    @Override
    public void addNode(@NotNull NetNode node) {
        nodeClassCheck(node);
        this.backer.addNode(node);
    }

    public @Nullable WorldNetNode getNode(@NotNull BlockPos equivalencyData) {
        return (WorldNetNode) getNode((Object) equivalencyData);
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
        double weight = source.getData().getLogicEntryDefaultable(WeightFactorLogic.TYPE).getValue() +
                target.getData().getLogicEntryDefaultable(WeightFactorLogic.TYPE).getValue();
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

    protected ResourceKey<Level> getDimension() {
        if (level == null) return fallbackDimension;
        else return level.dimension();
    }

    public void load(@NotNull CompoundTag nbt) {
        fallbackDimension = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(nbt.getString("Dimension")));
        backer.readFromNBT(nbt);
    }

    public @NotNull CompoundTag save(@NotNull CompoundTag compound) {
        compound.putString("Dimension", getDimension().location().toString());
        return backer.writeToNBT(compound);
    }

    @Override
    public GraphNetBacker getBacker() {
        return backer;
    }

    @Override
    public Class<? extends NetNode> getNodeClass() {
        return WorldNetNode.class;
    }

    @Override
    public @NotNull WorldNetNode getNewNode() {
        return new WorldNetNode(this);
    }
}
