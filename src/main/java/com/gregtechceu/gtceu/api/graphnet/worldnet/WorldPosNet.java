package com.gregtechceu.gtceu.api.graphnet.worldnet;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.alg.AlgorithmBuilder;
import com.gregtechceu.gtceu.api.graphnet.graph.INetGraph;

import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class WorldPosNet extends WorldNet {

    public WorldPosNet(@NotNull Function<IGraphNet, INetGraph> graphBuilder,
                       AlgorithmBuilder... algorithmBuilders) {
        super(graphBuilder, algorithmBuilders);
    }

    public WorldPosNet(boolean directed, AlgorithmBuilder... algorithmBuilders) {
        super(directed, algorithmBuilders);
    }

    @NotNull
    public WorldPosNetNode getOrCreateNode(@NotNull BlockPos pos) {
        WorldPosNetNode node = getNode(pos);
        if (node != null) return node;
        node = getNewNode();
        node.setPos(pos);
        addNode(node);
        return node;
    }

    public @Nullable WorldPosNetNode getNode(@NotNull BlockPos equivalencyData) {
        return (WorldPosNetNode) getNode((Object) equivalencyData);
    }

    @Override
    public Class<? extends NetNode> getNodeClass() {
        return WorldPosNetNode.class;
    }

    @Override
    public @NotNull WorldPosNetNode getNewNode() {
        return new WorldPosNetNode(this);
    }
}
