package com.gregtechceu.gtceu.common.pipelike.net.fluid;

import com.google.common.primitives.Ints;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.fluids.FluidState;
import com.gregtechceu.gtceu.api.fluids.GTFluid;
import com.gregtechceu.gtceu.api.fluids.attribute.FluidAttribute;
import com.gregtechceu.gtceu.api.graphnet.IGraphNet;
import com.gregtechceu.gtceu.api.graphnet.NetNode;
import com.gregtechceu.gtceu.api.graphnet.edge.AbstractNetFlowEdge;
import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.pipenet.FlowWorldPipeNetPath;
import com.gregtechceu.gtceu.api.graphnet.pipenet.NodeLossCache;
import com.gregtechceu.gtceu.api.graphnet.pipenet.NodeLossResult;
import com.gregtechceu.gtceu.api.graphnet.pipenet.WorldPipeNetNode;
import com.gregtechceu.gtceu.api.graphnet.pipenet.logic.TemperatureLogic;
import com.gregtechceu.gtceu.api.graphnet.pipenet.physical.tile.IWorldPipeNetTile;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.FluidTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.AbstractTraverseData;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.MultLossOperator;
import com.gregtechceu.gtceu.api.graphnet.traverse.util.ReversibleLossOperator;
import com.gregtechceu.gtceu.utils.EntityDamageUtil;

import com.lowdragmc.lowdraglib.side.fluid.FluidHelper;
import com.lowdragmc.lowdraglib.side.fluid.FluidStack;
import com.lowdragmc.lowdraglib.side.fluid.FluidTransferHelper;
import com.lowdragmc.lowdraglib.side.fluid.IFluidTransfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class FluidTraverseData extends AbstractTraverseData<WorldPipeNetNode, FlowWorldPipeNetPath> {

    public static final float TEMPERATURE_EFFECT = 0.05f;

    static {
        ContainmentFailure.init();
    }

    protected final BlockPos sourcePos;
    protected final Direction inputFacing;

    protected final FluidStack stack;
    protected final FluidState state;
    protected final int fluidTemp;
    protected final boolean gaseous;
    protected final @Nullable Collection<FluidAttribute> attributes;

    public FluidTraverseData(IGraphNet net, FluidTestObject testObject, SimulatorKey simulator, long queryTick,
                             BlockPos sourcePos, Direction inputFacing) {
        super(net, testObject, simulator, queryTick);
        this.sourcePos = sourcePos;
        this.inputFacing = inputFacing;
        this.stack = testObject.recombine();
        this.state = FluidState.inferState(stack);
        this.fluidTemp = FluidHelper.getTemperature(stack);
        this.gaseous = stack.getFluid().getFluidType().isLighterThanAir();
        if (stack.getFluid() instanceof GTFluid at) {
            attributes = at.getAttributes();
        } else attributes = null;
    }

    @Override
    public FluidTestObject getTestObject() {
        return (FluidTestObject) super.getTestObject();
    }

    @Override
    public boolean prepareForPathWalk(@NotNull FlowWorldPipeNetPath path, long flow) {
        return flow <= 0;
    }

    @Override
    public ReversibleLossOperator traverseToNode(@NotNull WorldPipeNetNode node, long flowReachingNode) {
        NodeLossCache.Key key = NodeLossCache.key(node, this);
        NodeLossResult result = NodeLossCache.getLossResult(key);
        if (result != null) {
            return result.getLossFunction();
        } else {
            FluidContainmentLogic containmentLogic = node.getData()
                    .getLogicEntryDefaultable(FluidContainmentLogic.TYPE);

            TemperatureLogic temperatureLogic = node.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
            if (temperatureLogic != null) {
                result = temperatureLogic.getLossResult(getQueryTick());
                boolean overMax = fluidTemp > containmentLogic.getMaximumTemperature() &&
                        !(state == FluidState.PLASMA && containmentLogic.contains(FluidState.PLASMA));
                if (overMax) {
                    result = NodeLossResult.combine(result, new NodeLossResult(GTValues.RNG.nextInt(4) == 0 ? pipe -> {
                        IWorldPipeNetTile tile = pipe.getBlockEntityNoLoading();
                        if (tile != null) {
                            tile.playLossSound();
                            tile.spawnParticles(Direction.UP, ParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));
                            tile.dealAreaDamage(gaseous ? 2 : 1,
                                    entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                            fluidTemp, 2.0F, 10));
                            tile.setNeighborsToFire();
                        }
                    } : pipe -> {
                        IWorldPipeNetTile tile = pipe.getBlockEntityNoLoading();
                        if (tile != null) {
                            tile.playLossSound();
                            tile.spawnParticles(Direction.UP, ParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));
                            tile.dealAreaDamage(gaseous ? 2 : 1,
                                    entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                            fluidTemp, 2.0F, 10));
                        }
                    }, MultLossOperator.EIGHTHS[2]));
                } else if (temperatureLogic.isUnderMinimum(fluidTemp)) {
                    result = NodeLossResult.combine(result, new NodeLossResult(pipe -> {
                        IWorldPipeNetTile tile = pipe.getBlockEntityNoLoading();
                        if (tile != null) {
                            tile.playLossSound();
                            tile.spawnParticles(Direction.UP, ParticleTypes.CLOUD, 3 + GTValues.RNG.nextInt(2));
                            tile.dealAreaDamage(gaseous ? 2 : 1,
                                    entity -> EntityDamageUtil.applyTemperatureDamage(entity,
                                            fluidTemp, 2.0F, 10));
                        }
                    }, MultLossOperator.EIGHTHS[2]));
                }
            }

            if (!containmentLogic.contains(state)) {
                result = NodeLossResult.combine(result, ContainmentFailure.getFailure(state).computeLossResult(stack));
            }

            if (attributes != null) {
                for (FluidAttribute attribute : attributes) {
                    if (!containmentLogic.contains(attribute)) {
                        result = NodeLossResult.combine(result,
                                ContainmentFailure.getFailure(attribute).computeLossResult(stack));
                    }
                }
            }

            if (result == null) return ReversibleLossOperator.IDENTITY;
            NodeLossCache.registerLossResult(key, result);
            return result.getLossFunction();
        }
    }

    @Override
    public long finalizeAtDestination(@NotNull WorldPipeNetNode destination, long flowReachingDestination) {
        long availableFlow = flowReachingDestination;
        for (var capability : destination.getBlockEntity().getTargetsWithCapabilities(destination).entrySet()) {
            if (destination.getEquivalencyData().equals(sourcePos) &&
                    capability.getKey() == inputFacing)
                continue; // anti insert-to-our-source logic

            IFluidTransfer container = FluidTransferHelper.getFluidTransfer(capability.getValue().getLevel(),
                    capability.getValue().getBlockPos(), capability.getKey().getOpposite());
            if (container != null) {
                availableFlow -= IFluidTransferController.CONTROL.get(destination.getBlockEntity().getCoverHolder()
                        .getCoverAtSide(capability.getKey())).insertToHandler(getTestObject(),
                                (int) Math.min(Integer.MAX_VALUE, availableFlow), container, getSimulatorKey() == null);
            }
        }
        return flowReachingDestination - availableFlow;
    }

    @Override
    public void consumeFlowLimit(@NotNull AbstractNetFlowEdge edge, NetNode targetNode,
                                 long consumption) {
        super.consumeFlowLimit(edge, targetNode, consumption);
        if (consumption > 0 && !simulating()) {
            recordFlow(targetNode, consumption);
            TemperatureLogic temperatureLogic = targetNode.getData().getLogicEntryNullable(TemperatureLogic.TYPE);
            if (temperatureLogic != null) {
                FluidContainmentLogic containmentLogic = targetNode.getData()
                        .getLogicEntryDefaultable(FluidContainmentLogic.TYPE);
                boolean overMax = fluidTemp > containmentLogic.getMaximumTemperature() &&
                        !(state == FluidState.PLASMA && containmentLogic.contains(FluidState.PLASMA));
                temperatureLogic.moveTowardsTemperature(fluidTemp,
                        getQueryTick(), consumption * TEMPERATURE_EFFECT, !overMax);
            }
        }
    }

    private void recordFlow(@NotNull NetNode node, long flow) {
        FluidFlowLogic logic = node.getData().getLogicEntryNullable(FluidFlowLogic.TYPE);
        if (logic == null) {
            logic = FluidFlowLogic.TYPE.getNew();
            node.getData().setLogicEntry(logic);
        }
        logic.recordFlow(getQueryTick(), getTestObject().recombine(Ints.saturatedCast(flow)));
    }
}
