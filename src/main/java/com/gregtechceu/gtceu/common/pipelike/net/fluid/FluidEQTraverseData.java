package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.IEqualizableTraverseData;

import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidTransferHelperImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import org.jetbrains.annotations.NotNull;

public class FluidEQTraverseData extends FluidTraverseData
                                 implements IEqualizableTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    protected int destCount;
    protected long maxMinFlow;

    public FluidEQTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                               BlockPos sourcePos, Direction inputFacing) {
        super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
    }

    protected void compute(@NotNull WorldPipeNetNode destination) {
        this.destCount = 0;
        this.maxMinFlow = 0;
        for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination.getEquivalencyData().equals(sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            IFluidTransfer container = FluidTransferHelper.getFluidTransfer(capability.getValue().getLevel(),
                    capability.getValue().getBlockPos(), capability.getKey().getOpposite());
            if (container != null) {
                if (destCount == 0) maxMinFlow = Integer.MAX_VALUE;
                destCount += 1;
                maxMinFlow = Math.min(maxMinFlow,
                        IFluidTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                                .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                        Integer.MAX_VALUE,
                                        container, false));
            }
        }
    }

    @Override
    public int getDestinationsAtNode(@NotNull WorldPipeNetNode node) {
        return destCount;
    }

    @Override
    public boolean shouldSkipPath(@NotNull FlowWorldPipeNetPath path) {
        compute(path.getTargetNode());
        return maxMinFlow == 0;
    }

    @Override
    public long getMaxFlowToLeastDestination(@NotNull WorldPipeNetNode destination) {
        return maxMinFlow;
    }

    @Override
    public long finalizeAtDestination(@NotNull WorldPipeNetNode node, long flowReachingNode, int expectedDestinations) {
        long availableFlow = flowReachingNode;
        long flowPerDestination = flowReachingNode / expectedDestinations;
        if (flowPerDestination == 0) return 0;
        for (var capability : node.getBlockEntity().getTargetsWithCapabilities(node).entrySet()) {
            if (node.getEquivalencyData().equals(sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            var containerCap = capability.getValue()
                    .getCapability(ForgeCapabilities.FLUID_HANDLER, capability.getKey().getOpposite()).resolve()
                    .orElse(null);
            if (containerCap != null) {
                IFluidTransfer container = FluidTransferHelperImpl.toFluidTransfer(containerCap);
                availableFlow -= IFluidTransferController.CONTROL.get(node.getBlockEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, flowPerDestination), container, !simulating());
            }
        }
        return flowReachingNode - availableFlow;
    }
}