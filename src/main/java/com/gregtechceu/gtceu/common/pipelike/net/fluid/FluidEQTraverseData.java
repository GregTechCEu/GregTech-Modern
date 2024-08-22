package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.IEqualizableTraverseData;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.fluids.capability.IFluidHandler;
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
}
