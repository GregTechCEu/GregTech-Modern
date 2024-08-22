package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.graphnet.AbstractGroupData;
import com.gregtechceu.gtceu.api.graphnet.NetGroup;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.logic.NetLogicData;
import com.gregtechceu.gtceu.api.graphnet.logic.ThroughputLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNet;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.IPipeCapabilityObject;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.PipeBlockEntity;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.TraverseHelpers;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.Platform;

import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Iterator;

public class EnergyCapabilityObject implements IPipeCapabilityObject, IEnergyContainer {

    private final WorldPipeNet net;
    @Setter
    private @Nullable PipeBlockEntity tile;

    private final EnumMap<Direction, AbstractNetFlowEdge> internalBuffers = new EnumMap<>(Direction.class);
    private final WorldPipeNetNode node;

    private boolean transferring = false;

    public <N extends WorldPipeNet & FlowWorldPipeNetPath.Provider> EnergyCapabilityObject(@NotNull N net,
                                                                                           WorldPipeNetNode node) {
        this.net = net;
        this.node = node;
        for (Direction facing : GTUtil.DIRECTIONS) {
            AbstractNetFlowEdge edge = (AbstractNetFlowEdge) net.getNewEdge();
            edge.setData(NetLogicData.union(node.getData(), (NetLogicData) null));
            internalBuffers.put(facing, edge);
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

    @Override
    public long acceptEnergyFromNetwork(Direction side, long voltage, long amperage, boolean simulate) {
        if (tile == null || this.transferring || inputDisallowed(side)) return 0;
        this.transferring = true;

        SimulatorKey simulator = null;
        if (simulate) simulator = SimulatorKey.getNewSimulatorInstance();
        long tick = Platform.getMinecraftServer().getTickCount();

        AbstractNetFlowEdge internalBuffer = this.internalBuffers.get(side);
        if (internalBuffer != null) {
            long limit = internalBuffer.getFlowLimit(IPredicateTestObject.INSTANCE, net, tick, simulator);
            if (limit <= 0) {
                this.transferring = false;
                return 0;
            } else if (amperage > limit) {
                amperage = limit;
            }
        }
        long availableAmperage = amperage;

        EnergyTraverseData data = new EnergyTraverseData(net, IPredicateTestObject.INSTANCE, simulator, tick, voltage,
                tile.getBlockPos(), side);
        availableAmperage -= TraverseHelpers.traverseFlood(data, getPaths(data), availableAmperage);
        if (availableAmperage > 0) {
            availableAmperage -= TraverseHelpers.traverseDumb(data, getPaths(data), data::handleOverflow,
                    availableAmperage);
        }
        long accepted = amperage - availableAmperage;

        if (internalBuffer != null) data.consumeFlowLimit(internalBuffer, node, accepted);
        if (!simulate) {
            EnergyGroupData group = getEnergyData();
            if (group != null) {
                group.addEnergyInPerSec(accepted * voltage, data.getQueryTick());
            }
        }
        this.transferring = false;
        return accepted;
    }

    private Iterator<FlowWorldPipeNetPath> getPaths(EnergyTraverseData data) {
        assert tile != null;
        return getProvider().getPaths(net.getNode(tile.getBlockPos()), data.getTestObject(), data.getSimulatorKey(),
                data.getQueryTick());
    }

    @Nullable
    private EnergyGroupData getEnergyData() {
        if (tile == null) return null;
        NetNode node = net.getNode(tile.getBlockPos());
        if (node == null) return null;
        NetGroup group = node.getGroupUnsafe();
        if (group == null) return null;
        AbstractGroupData data = group.getData();
        if (!(data instanceof EnergyGroupData e)) return null;
        return e;
    }

    @Override
    public long getInputAmperage() {
        if (tile == null) return 0;
        return tile.getNetLogicData(net.getNetworkID()).getLogicEntryDefaultable(ThroughputLogic.INSTANCE).getValue();
    }

    @Override
    public long getInputVoltage() {
        if (tile == null) return 0;
        return tile.getNetLogicData(net.getNetworkID()).getLogicEntryDefaultable(VoltageLimitLogic.INSTANCE).getValue();
    }

    @Override
    public Capability<?>[] getCapabilities() {
        return WorldEnergyNet.CAPABILITIES;
    }

    @Override
    public <T> LazyOptional<T> getCapabilityForSide(Capability<T> capability, @Nullable Direction facing) {
        if (capability == GTCapability.CAPABILITY_ENERGY_CONTAINER) {
            return GTCapability.CAPABILITY_ENERGY_CONTAINER
                    .orEmpty(capability, LazyOptional.of(() -> this));
        }
        return null;
    }

    @Override
    public long getInputPerSec() {
        EnergyGroupData data = getEnergyData();
        if (data == null) return 0;
        else return data
                .getEnergyInPerSec(Platform.getMinecraftServer().getTickCount());
    }

    @Override
    public long getOutputPerSec() {
        EnergyGroupData data = getEnergyData();
        if (data == null) return 0;
        else return data
                .getEnergyOutPerSec(Platform.getMinecraftServer().getTickCount());
    }

    @Override
    public boolean inputsEnergy(Direction side) {
        return !inputDisallowed(side);
    }

    @Override
    public boolean outputsEnergy(Direction side) {
        return true;
    }

    @Override
    public long changeEnergy(long differenceAmount) {
        GTCEu.LOGGER.error("Do not use changeEnergy() for cables! Use acceptEnergyFromNetwork()");
        return acceptEnergyFromNetwork(null,
                differenceAmount / getInputAmperage(),
                differenceAmount / getInputVoltage(), false) * getInputVoltage();
    }

    @Override
    public long getEnergyStored() {
        return 0;
    }

    @Override
    public long getEnergyCapacity() {
        return getInputAmperage() * getInputVoltage();
    }

    @Override
    public boolean isOneProbeHidden() {
        return true;
    }
}
