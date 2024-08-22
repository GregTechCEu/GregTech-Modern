package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.IRoundRobinTraverseData;

import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidTransferHelperImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.capability.IFluidHandler;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import org.jetbrains.annotations.NotNull;

public class FluidRRTraverseData extends FluidTraverseData implements
                                 IRoundRobinTraverseData<SimpleTileRoundRobinData<IFluidHandler>, WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> cache;

    public FluidRRTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                               BlockPos sourcePos, Direction inputFacing,
                               @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> cache) {
        super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        this.cache = cache;
    }

    @Override
    public @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IFluidHandler>> getTraversalCache() {
        return cache;
    }

    @Override
    public boolean shouldSkipPath(@NotNull FlowWorldPipeNetPath path) {
        return false;
    }

    @Override
    public @NotNull SimpleTileRoundRobinData<IFluidHandler> createRRData(@NotNull WorldPipeNetNode destination) {
        return new SimpleTileRoundRobinData<>(ForgeCapabilities.FLUID_HANDLER);
    }

    @Override
    public long finalizeAtDestination(@NotNull SimpleTileRoundRobinData<IFluidHandler> data,
                                      @NotNull WorldPipeNetNode destination, long flowReachingDestination) {
        long availableFlow = flowReachingDestination;
        Direction pointerFacing = data.getPointerFacing(getSimulatorKey());
        // anti insert-to-our-source logic
        if (!destination.getEquivalencyData().equals(sourcePos) || !(pointerFacing == inputFacing)) {
            IFluidHandler handler = data.getAtPointer(destination, getSimulatorKey());
            if (handler != null) {
                IFluidTransfer container = FluidTransferHelperImpl.toFluidTransfer(handler);
                availableFlow -= IFluidTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                        .getCoverAtSide(pointerFacing)).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, availableFlow), container, getSimulatorKey() == null);
            }
        }
        return flowReachingDestination - availableFlow;
    }
}
