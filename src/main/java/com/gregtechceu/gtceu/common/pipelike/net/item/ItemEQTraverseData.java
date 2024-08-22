package com.gregtechceu.gtceu.common.pipelike.net.item;

import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.IEqualizableTraverseData;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

public class ItemEQTraverseData extends ItemTraverseData
                                implements IEqualizableTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    protected int destCount;
    protected int maxMinFlow;

    public ItemEQTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
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

            IItemHandler cap = capability.getValue()
                    .getCapability(ForgeCapabilities.ITEM_HANDLER, capability.getKey().getOpposite()).resolve()
                    .orElse(null);
            if (cap != null) {
                IItemTransfer container = ItemTransferHelperImpl.toItemTransfer(cap);
                if (destCount == 0) maxMinFlow = Integer.MAX_VALUE;
                destCount += 1;
                int test = Integer.MAX_VALUE;
                maxMinFlow = Math.min(maxMinFlow, test -
                        IItemTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                                .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(), test,
                                        container, true));
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
