package com.gregtechceu.gtceu.common.pipelike.net.item;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.traverse.SimpleTileRoundRobinData;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.IRoundRobinTraverseData;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

public class ItemRRTraverseData extends ItemTraverseData implements
        IRoundRobinTraverseData<SimpleTileRoundRobinData<IItemHandler>, WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> cache;

    public ItemRRTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                              BlockPos sourcePos, Direction inputFacing,
                              @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> cache) {
        super(net, testObject, simulator, queryTick, sourcePos, inputFacing);
        this.cache = cache;
    }

    @Override
    public @NotNull Object2ObjectLinkedOpenHashMap<Object, SimpleTileRoundRobinData<IItemHandler>> getTraversalCache() {
        return cache;
    }

    @Override
    public boolean shouldSkipPath(@NotNull FlowWorldPipeNetPath path) {
        return false;
    }

    @Override
    public @NotNull SimpleTileRoundRobinData<IItemHandler> createRRData(@NotNull WorldPipeNetNode destination) {
        return new SimpleTileRoundRobinData<>(ForgeCapabilities.ITEM_HANDLER);
    }

    @Override
    public long finalizeAtDestination(@NotNull SimpleTileRoundRobinData<IItemHandler> data,
                                      @NotNull WorldPipeNetNode destination,
                                      long flowReachingDestination) {
        long availableFlow = flowReachingDestination;
        Direction pointerFacing = data.getPointerFacing(getSimulatorKey());
        if (destination.getEquivalencyData().equals(sourcePos) && pointerFacing == inputFacing)
            return 0; // anti insert-to-our-source logic

        IItemHandler cap = data.getAtPointer(destination, getSimulatorKey());
        if (cap != null) {
            IItemTransfer container = ItemTransferHelperImpl.toItemTransfer(cap);
            availableFlow = IItemTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                    .getCoverAtSide(pointerFacing)).insertToHandler(getTestObject(),
                            (int) Math.min(Integer.MAX_VALUE, availableFlow), container, getSimulatorKey() != null);
        }
        return flowReachingDestination - availableFlow;
    }
}
