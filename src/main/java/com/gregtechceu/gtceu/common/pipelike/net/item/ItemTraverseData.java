package com.gregtechceu.gtceu.common.pipelike.net.item;

import com.google.common.primitives.Ints;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.AbstractTraverseData;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.ReversibleLossOperator;

import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import org.jetbrains.annotations.NotNull;

public class ItemTraverseData extends AbstractTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    protected final BlockPos sourcePos;
    protected final Direction inputFacing;

    public ItemTraverseData(IGraphNet net, ItemTestObject testObject, SimulatorKey simulator, long queryTick,
                            BlockPos sourcePos, Direction inputFacing) {
        super(net, testObject, simulator, queryTick);
        this.sourcePos = sourcePos;
        this.inputFacing = inputFacing;
    }

    @Override
    public ItemTestObject getTestObject() {
        return (ItemTestObject) super.getTestObject();
    }

    @Override
    public boolean prepareForPathWalk(@NotNull FlowWorldPipeNetPath path, long flow) {
        return flow <= 0;
    }

    @Override
    public ReversibleLossOperator traverseToNode(@NotNull WorldPipeNetNode node, long flowReachingNode) {
        return ReversibleLossOperator.IDENTITY;
    }

    @Override
    public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
        long availableFlow = flowReachingDestination;
        for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination.getEquivalencyData().equals(sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            IItemHandler cap = capability.getValue()
                    .getCapability(ForgeCapabilities.ITEM_HANDLER, capability.getKey().getOpposite()).resolve()
                    .orElse(null);
            if (cap != null) {
                IItemTransfer container = ItemTransferHelperImpl.toItemTransfer(cap);
                availableFlow = IItemTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, availableFlow), container, getSimulatorKey() != null);
            }
        }
        return flowReachingDestination - availableFlow;
    }

    @Override
    public void consumeFlowLimit(@NotNull AbstractNetFlowEdge edge, NetNode targetNode, long consumption) {
        super.consumeFlowLimit(edge, targetNode, consumption);
        if (consumption > 0 && !simulating()) {
            recordFlow(targetNode, consumption);
        }
    }

    private void recordFlow(@NotNull NetNode node, long flow) {
        ItemFlowLogic logic = node.getData().getLogicEntryNullable(ItemFlowLogic.TYPE);
        if (logic == null) {
            logic = ItemFlowLogic.TYPE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(getQueryTick(), getTestObject().recombine(Ints.saturatedCast(flow)));
    }

}
