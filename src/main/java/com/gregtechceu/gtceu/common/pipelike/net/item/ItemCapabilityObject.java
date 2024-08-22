package com.gregtechceu.gtceu.common.pipelike.net.item;

import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.ItemTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.ITraverseData;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseDataProvider;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseGuide;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseHelpers;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.side.item.IItemTransfer;
import com.lowdragmc.lowdraglib.side.item.forge.ItemTransferHelperImpl;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.function.LongConsumer;

public class ItemCapabilityObject implements IPipeCapabilityObject, IItemTransfer, IItemTraverseGuideProvider {

    private final WorldPipeNet net;
    @Setter
    private @Nullable PipeBlockEntity tile;

    private final EnumMap<Direction, Wrapper> wrappers = new EnumMap<>(Direction.class);
    private final WorldPipeNetNode node;

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> ItemCapabilityObject(@NotNull N net,
                                                                                         WorldPipeNetNode node) {
        this.net = net;
        this.node = node;
        for (Direction facing : GTUtil.DIRECTIONS) {
            AbstractNetFlowEdge edge = (AbstractNetFlowEdge) net.getNewEdge();
            edge.setData(NetLogicData.union(node.getData(), (NetLogicData) null));
            wrappers.put(facing, new Wrapper(facing, edge));
        }
    }

    private FlowWorldPipeNetPath.Provider getProvider() {
        return (FlowWorldPipeNetPath.Provider) net;
    }

    private boolean inputDisallowed(Direction side) {
        if (side == null) return false;
        if (tile == null) return true;
        else return tile.isBlocked(side);
    }

    private Iterator<FlowWorldPipeNetPath> getPaths(@NotNull ITraverseData<?, ?> data) {
        assert tile != null;
        return getProvider().getPaths(net.getNode(tile.getBlockPos()), data.getTestObject(), data.getSimulatorKey(),
                data.getQueryTick());
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldItemNet.CAPABILITIES;
    }

    @Override
    public <T> LazyOptional<T> getCapabilityForSide(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            return ForgeCapabilities.ITEM_HANDLER.orEmpty(capability, facing == null ?
                    LazyOptional.of(() -> ItemTransferHelperImpl.toItemHandler(this)) :
                    LazyOptional.of(() -> ItemTransferHelperImpl.toItemHandler(wrappers.get(facing))));
        }
        return null;
    }

    public @NotNull ItemStack insertItem(@NotNull ItemStack stack, boolean simulate, boolean notifyChanges,
                                         Direction side) {
        if (this.transferring) return stack;
        this.transferring = true;

        var guide = getGuide(ItemTraverseData::new, new ItemTestObject(stack), stack.getCount(), simulate, side);
        if (guide == null) return stack;
        int consumed = (int) TraverseHelpers.traverseFlood(guide.getData(), guide.getPaths(), guide.getFlow());
        guide.reportConsumedFlow(consumed);

        this.transferring = false;
        return guide.getData().getTestObject().recombine(stack.getCount() - consumed);
    }

    @Nullable
    @Override
    public <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                               ItemTestObject testObject,
                                                                                                                                               long flow,
                                                                                                                                               boolean simulate) {
        return getGuide(provider, testObject, flow, simulate, null);
    }

    @Nullable
    protected <
            D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                               ItemTestObject testObject,
                                                                                                                                               long flow,
                                                                                                                                               boolean simulate,
                                                                                                                                               Direction side) {
        if (tile == null || inputDisallowed(side)) return null;

        SimulatorKey simulator = simulate ? SimulatorKey.getNewSimulatorInstance() : null;
        long tick = Platform.getMinecraftServer().getTickCount();
        D data = provider.of(net, testObject, simulator, tick, tile.getBlockPos(), side);

        LongConsumer flowReport = null;
        Wrapper wrapper = this.wrappers.get(side);
        if (wrapper != null) {
            AbstractNetFlowEdge internalBuffer = wrapper.getBuffer();
            if (internalBuffer != null) {
                long limit = internalBuffer.getFlowLimit(testObject, net, tick, simulator);
                if (limit <= 0) {
                    this.transferring = false;
                    return null;
                }
                flow = Math.min(limit, flow);
                flowReport = l -> data.consumeFlowLimit(internalBuffer, node, l);
            }
        }
        return new TraverseGuide<>(data, () -> getPaths(data), flow, flowReport);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
        return insertItem(stack, simulate, notifyChanges, null);
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return false;
    }

    // Unused
    @NotNull
    @Override
    public Object createSnapshot() {
        return new Object();
    }

    // Unused
    @Override
    public void restoreFromSnapshot(Object snapshot) {}

    protected class Wrapper implements IItemTransfer, IItemTraverseGuideProvider {

        private final Direction facing;
        @Getter
        private final AbstractNetFlowEdge buffer;

        public Wrapper(Direction facing, AbstractNetFlowEdge buffer) {
            this.facing = facing;
            this.buffer = buffer;
        }

        @Nullable
        @Override
        public <D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                                   TraverseDataProvider<D, ItemTestObject> provider,
                                                                                                                                                   ItemTestObject testObject,
                                                                                                                                                   long flow,
                                                                                                                                                   boolean simulate) {
            return ItemCapabilityObject.this.getGuide(provider, testObject, flow, simulate, facing);
        }

        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate, boolean notifyChanges) {
            return ItemCapabilityObject.this.insertItem(stack, simulate, notifyChanges, facing);
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate, boolean notifyChanges) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return false;
        }

        // Unused
        @NotNull
        @Override
        public Object createSnapshot() {
            return new Object();
        }

        // Unused
        @Override
        public void restoreFromSnapshot(Object snapshot) {}
    }
}
