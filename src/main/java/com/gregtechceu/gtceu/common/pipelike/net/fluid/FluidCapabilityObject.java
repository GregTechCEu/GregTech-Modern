package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.logic.ChannelCountLogic;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.ITraverseData;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseDataProvider;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseGuide;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseHelpers;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.Platform;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;
import com.lowdragmc.lowdraglib.side.fluid.forge.FluidTransferHelperImpl;

import net.minecraft.core.Direction;
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

public class FluidCapabilityObject implements IPipeCapabilityObject, IFluidTransfer,
                                   IFluidTraverseGuideProvider {

    private final WorldPipeNet net;
    @Getter
    @Setter
    private @Nullable PipeBlockEntity tile;
    @Getter
    private final int tanks;

    private final EnumMap<Direction, Wrapper> wrappers = new EnumMap<>(Direction.class);
    private final WorldPipeNetNode node;

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> FluidCapabilityObject(@NotNull N net,
                                                                                          WorldPipeNetNode node) {
        this.net = net;
        this.node = node;
        this.tanks = node.getData().getLogicEntryDefaultable(ChannelCountLogic.TYPE)
                .getValue();
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
        return WorldFluidNet.CAPABILITIES;
    }

    @Override
    public <T> LazyOptional<T> getCapabilityForSide(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            if (facing == null) return LazyOptional.empty(); // hard override to prevent TOP from displaying a tank.
            //noinspection ConstantValue
            return ForgeCapabilities.FLUID_HANDLER.orEmpty(capability, facing == null ?
                    LazyOptional.of(() -> FluidTransferHelperImpl.toFluidHandler(this)) :
                    LazyOptional.of(() -> FluidTransferHelperImpl.toFluidHandler(wrappers.get(facing))));
        }
        return null;
    }

    @Override
    public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return fill(resource, simulate, null);
    }

    public int fill(FluidStack resource, boolean simulate, Direction side) {
        if (this.transferring) return 0;
        this.transferring = true;

        var guide = getGuide(FluidTraverseData::new, new FluidTestObject(resource), resource.getAmount(), simulate,
                side);
        if (guide == null) return 0;
        int accepted = (int) TraverseHelpers.traverseFlood(guide.getData(), guide.getPaths(), guide.getFlow());
        guide.reportConsumedFlow(accepted);

        this.transferring = false;
        return accepted;
    }

    @NotNull
    @Override
    public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
        return FluidStack.empty();
    }

    @Override
    public @Nullable <
            D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                               FluidTestObject testObject,
                                                                                                                                               long flow,
                                                                                                                                               boolean simulate) {
        return getGuide(provider, testObject, flow, simulate, null);
    }

    public @Nullable <
            D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                               TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                               FluidTestObject testObject,
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

    @NotNull
    @Override
    public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public Object createSnapshot() {
        return null;
    }

    @Override
    public void restoreFromSnapshot(Object snapshot) {}

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, boolean simulate, boolean notifyChanges) {
        return FluidStack.empty();
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {
        return FluidStack.empty();
    }

    @Override
    public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {}

    @Override
    public long getTankCapacity(int tank) {
        return Long.MAX_VALUE;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public boolean supportsFill(int tank) {
        return true;
    }

    @Override
    public boolean supportsDrain(int tank) {
        return false;
    }

    protected class Wrapper implements IFluidTransfer, IFluidTraverseGuideProvider {

        private final Direction facing;
        @Getter
        private final AbstractNetFlowEdge buffer;

        public Wrapper(Direction facing, AbstractNetFlowEdge buffer) {
            this.facing = facing;
            this.buffer = buffer;
        }

        @Override
        public @Nullable <
                D extends ITraverseData<WorldPipeNetNode, FlowWorldPipeNetPath>> TraverseGuide<WorldPipeNetNode, FlowWorldPipeNetPath, D> getGuide(
                                                                                                                                                   TraverseDataProvider<D, FluidTestObject> provider,
                                                                                                                                                   FluidTestObject testObject,
                                                                                                                                                   long flow,
                                                                                                                                                   boolean simulate) {
            return FluidCapabilityObject.this.getGuide(provider, testObject, flow, simulate, facing);
        }

        @Override
        public int getTanks() {
            return FluidCapabilityObject.this.tanks;
        }

        @Override
        public long fill(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return FluidCapabilityObject.this.fill(resource, simulate, facing);
        }

        @NotNull
        @Override
        public FluidStack drain(long maxDrain, boolean simulate, boolean notifyChanges) {
            return FluidStack.empty();
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, boolean simulate, boolean notifyChanges) {
            return FluidStack.empty();
        }

        @NotNull
        @Override
        public FluidStack drain(int tank, FluidStack resource, boolean simulate, boolean notifyChanges) {
            return FluidStack.empty();
        }

        // Unused.
        @NotNull
        @Override
        public Object createSnapshot() {
            return new Object();
        }

        // Unused.
        @Override
        public void restoreFromSnapshot(Object snapshot) {}

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.empty();
        }

        @Override
        public void setFluidInTank(int tank, @NotNull FluidStack fluidStack) {}

        @Override
        public long getTankCapacity(int tank) {
            return (int) Math.min(Integer.MAX_VALUE, buffer.getThroughput());
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return true;
        }

        @Override
        public boolean supportsFill(int tank) {
            return true;
        }

        @Override
        public boolean supportsDrain(int tank) {
            return false;
        }
    }
}
