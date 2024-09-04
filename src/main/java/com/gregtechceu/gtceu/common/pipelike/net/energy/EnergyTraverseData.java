package com.gregtechceu.gtceu.common.pipelike.net.energy;

import com.gregtechceu.gtceu.api.capability.IEnergyContainer;
import com.gregtechceu.gtceu.api.capability.forge.GTCapability;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.NodeLossCache;
import com.gregtechceu.gtceu.api.graphnet.pipenet.NodeLossResult;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLogic;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.AbstractTraverseData;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.ReversibleLossOperator;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.LongSupplier;

public class EnergyTraverseData extends AbstractTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    private final Object2ObjectOpenHashMap<WorldPipeNetNode, OverVoltageInformation> overVoltageInformation;

    private final long startVoltage;
    private long pathVoltage;

    private final BlockPos sourcePos;
    private final Direction inputFacing;

    public EnergyTraverseData(IGraphNet net, IPredicateTestObject testObject, SimulatorKey simulator, long queryTick,
                              long startVoltage, BlockPos sourcePos, Direction inputFacing) {
        super(net, testObject, simulator, queryTick);
        this.overVoltageInformation = new Object2ObjectOpenHashMap<>();
        this.startVoltage = startVoltage;
        this.sourcePos = sourcePos;
        this.inputFacing = inputFacing;
    }

    @Override
    public boolean prepareForPathWalk(@NotNull FlowWorldPipeNetPath path, long flow) {
        if (flow <= 0) return true;
        this.pathVoltage = startVoltage;
        this.overVoltageInformation.clear();
        this.overVoltageInformation.trim(10);
        return false;
    }

    @Override
    public ReversibleLossOperator traverseToNode(@NotNull WorldPipeNetNode node, long flowReachingNode) {
        VoltageLimitLogic limitLogic = node.getData().getLogicEntryNullable(VoltageLimitLogic.TYPE);
        if (limitLogic != null) {
            long voltage = limitLogic.getValue();
            if (voltage < pathVoltage) overVoltageInformation.put(node,
                    new OverVoltageInformation(voltage, flowReachingNode));
        }
        TemperatureLogic temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
        if (!node.getData().getLogicEntryDefaultable(SuperconductorLogic.TYPE)
                .canSuperconduct(temperatureLogic == null ? TemperatureLogic.DEFAULT_TEMPERATURE :
                        temperatureLogic.getTemperature(getQueryTick()))) {
            pathVoltage -= node.getData().getLogicEntryDefaultable(VoltageLossLogic.INSTANCE).getValue();
        }

        NodeLossCache.Key key = NodeLossCache.key(node, this);
        NodeLossResult result = NodeLossCache.getLossResult(key);
        if (result != null) {
            return result.getLossFunction();
        } else {
            result = temperatureLogic == null ? null : temperatureLogic.getLossResult(getQueryTick());
            if (result == null) {
                return ReversibleLossOperator.IDENTITY;
            }
            if (result.hasPostAction()) NodeLossCache.registerLossResult(key, result);
            return result.getLossFunction();
        }
    }

    public void handleOverflow(@NotNull WorldPipeNetNode node, long overflow) {
        TemperatureLogic logic = (TemperatureLogic) node.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
        if (logic != null) {
            // this occurs after finalization but before path reset.
            logic.applyThermalEnergy(calculateHeatA(overflow, pathVoltage), getQueryTick());
        }
    }

    @Override
    public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
        this.pathVoltage = (long) GTUtil.geometricMean(pathVoltage,
                overVoltageInformation.values().stream().filter(o -> o.voltageCap < this.pathVoltage)
                        .mapToDouble(o -> (double) o.voltageCap).toArray());
        overVoltageInformation.forEach((k, v) -> v.doHeating(k, pathVoltage, getQueryTick()));
        long availableFlow = flowReachingDestination;
        for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination.getEquivalencyData().equals(sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            IEnergyContainer container = capability.getValue()
                    .getCapability(GTCapability.CAPABILITY_ENERGY_CONTAINER, capability.getKey().getOpposite())
                    .resolve().orElse(null);
            if (container != null) {
                availableFlow -= IEnergyTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(pathVoltage, availableFlow, container,
                                capability.getKey(), getSimulatorKey() != null);
            }
        }
        long accepted = flowReachingDestination - availableFlow;
        if (getSimulatorKey() == null && destination.getGroupUnsafe() != null &&
                destination.getGroupSafe().getData() instanceof EnergyGroupData data) {
            data.addEnergyOutPerSec(accepted * pathVoltage, getQueryTick());
        }
        return accepted;
    }

    @Override
    public void consumeFlowLimit(@NotNull AbstractNetFlowEdge edge, NetNode targetNode,
                                 long consumption) {
        super.consumeFlowLimit(edge, targetNode, consumption);
        if (consumption > 0) recordFlow(targetNode, consumption);
    }

    private void recordFlow(@NotNull NetNode node, long amperes) {
        EnergyFlowLogic logic = node.getData().getLogicEntryNullable(EnergyFlowLogic.TYPE);
        if (logic == null) {
            logic = EnergyFlowLogic.TYPE.supplier().get();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(getQueryTick(), new EnergyFlowData(amperes, pathVoltage));
    }

    private static int calculateHeatV(long amperage, long voltage, long maxVoltage) {
        return (int) (amperage * (Math.log1p(Math.log((double) voltage / maxVoltage)) * 45 + 36.5));
    }

    private static int calculateHeatA(long amperage, long voltage) {
        return (int) (amperage * (Math.log1p(Math.log((double) voltage)) * 45 + 36.5));
    }

    protected static class OverVoltageInformation implements LongSupplier {

        public final long voltageCap;

        private final long amperage;

        public OverVoltageInformation(long voltageCap, long amperage) {
            this.voltageCap = voltageCap;
            this.amperage = amperage;
        }

        @Override
        public long getAsLong() {
            return voltageCap;
        }

        public void doHeating(WorldPipeNetNode node, long finalVoltage, long tick) {
            TemperatureLogic logic = (TemperatureLogic) node.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
            if (logic != null) {
                logic.applyThermalEnergy(calculateHeatV(amperage, finalVoltage, voltageCap), tick);
            }
        }
    }
}
