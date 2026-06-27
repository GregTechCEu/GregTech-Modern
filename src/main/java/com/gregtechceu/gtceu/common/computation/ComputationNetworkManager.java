package com.gregtechceu.gtceu.common.computation;

import com.gregtechceu.gtceu.api.computation.ComputationConsumer;
import com.gregtechceu.gtceu.api.computation.ComputationPort;
import com.gregtechceu.gtceu.api.computation.ComputationProducer;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiPart;
import com.gregtechceu.gtceu.api.machine.trait.ComputationPortTrait;
import com.gregtechceu.gtceu.common.blockentity.OpticalPipeBlockEntity;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.research.NetworkSwitchMachine;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalPipeNet;
import com.gregtechceu.gtceu.common.pipelike.optical.OpticalRoutePath;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ComputationNetworkManager {

    private static final Map<ServerLevel, ComputationNetworkManager> MANAGERS = new Object2ObjectOpenHashMap<>();

    public static ComputationNetworkManager get(ServerLevel level) {
        return MANAGERS.computeIfAbsent(level, ComputationNetworkManager::new);
    }

    private final ServerLevel level;
    private final Set<ComputationPortTrait> ports = new HashSet<>();
    private final List<ComputationNetwork> networks = new ArrayList<>();
    private boolean topologyDirty = true;

    private ComputationNetworkManager(ServerLevel level) {
        this.level = level;
    }

    public void registerPort(ComputationPortTrait port) {
        ports.add(port);
        topologyDirty = true;
    }

    public void unregisterPort(ComputationPortTrait port) {
        ports.remove(port);
        topologyDirty = true;
    }

    public void markPortTopologyDirty(ComputationPortTrait port) {
        if (ports.contains(port)) {
            topologyDirty = true;
        }
    }

    public void markTopologyDirty() {
        topologyDirty = true;
    }

    public void tick() {
        if (topologyDirty) {
            rebuildNetworks();
        }
        for (ComputationNetwork network : networks) {
            network.solve();
        }
    }

    public boolean reserveDemand(ComputationConsumer consumer, int requestedCWUt) {
        for (ComputationNetwork network : networks) {
            if (network.contains(consumer)) {
                return network.reserveDemand(consumer, requestedCWUt);
            }
        }
        return false;
    }

    public int getNetWorkMaxCWUt(ComputationPortTrait computationPortTrait) {
        for (ComputationNetwork network : networks) {
            if (network.contains(computationPortTrait)) {
                return network.lastTotalOfferedCWUt;
            }
        }
        return 0;
    }

    public int getNetWorkUsedCWUt(ComputationPortTrait computationPortTrait) {
        for (ComputationNetwork network : networks) {
            if (network.contains(computationPortTrait)) {
                return network.lastAllocatedCWUt;
            }
        }
        return 0;
    }

    private void rebuildNetworks() {
        topologyDirty = false;
        networks.clear();
        if (ports.isEmpty()) return;

        Map<ComputationPortTrait, ComputationPortTrait> parent = new HashMap<>();
        Map<BlockPos, ComputationPortTrait> portsByPos = new HashMap<>();
        for (ComputationPortTrait port : ports) {
            parent.put(port, port);
            portsByPos.put(port.getPortPos(), port);
        }

        unionAdjacentPorts(parent, portsByPos);
        unionOpticalPorts(parent);
        unionNetworkSwitchPorts(parent);

        Map<ComputationPortTrait, List<ComputationPortTrait>> groups = new HashMap<>();
        for (ComputationPortTrait port : ports) {
            groups.computeIfAbsent(find(parent, port), ignored -> new ArrayList<>()).add(port);
        }

        for (List<ComputationPortTrait> group : groups.values()) {
            ComputationNetwork network = new ComputationNetwork();
            network.replaceNodes(group);
            networks.add(network);
        }
    }

    private void unionAdjacentPorts(Map<ComputationPortTrait, ComputationPortTrait> parent,
                                    Map<BlockPos, ComputationPortTrait> portsByPos) {
        for (ComputationPortTrait port : ports) {
            if (!port.getComputationPortPolicy().acceptsAdjacent()) continue;
            for (Direction direction : GTUtil.DIRECTIONS) {
                ComputationPortTrait other = portsByPos.get(port.getPortPos().relative(direction));
                if (other != null && other.getComputationPortPolicy().acceptsAdjacent()) {
                    union(parent, port, other);
                }
            }
        }
    }

    private void unionOpticalPorts(Map<ComputationPortTrait, ComputationPortTrait> parent) {
        for (ComputationPortTrait port : ports) {
            if (!port.getComputationPortPolicy().acceptsOptical()) continue;
            ComputationPortTrait target = findOpticalRouteTarget(port);
            if (target == null || target == port || !target.getComputationPortPolicy().acceptsOptical()) continue;
            if (!canUseComputationRoute(port, target)) continue;
            union(parent, port, target);
        }
    }

    private ComputationPortTrait findOpticalRouteTarget(ComputationPortTrait source) {
        BlockPos sourcePos = source.getPortPos();
        for (Direction direction : GTUtil.DIRECTIONS) {
            if (!source.hasCapability(direction)) continue;
            BlockPos pipePos = sourcePos.relative(direction);
            if (!(level.getBlockEntity(pipePos) instanceof OpticalPipeBlockEntity pipe)) continue;
            Direction pipeSideToSource = direction.getOpposite();
            if (!pipe.isConnected(pipeSideToSource)) continue;

            OpticalPipeNet pipeNet = pipe.getOpticalPipeNet();
            if (pipeNet == null) continue;

            OpticalRoutePath routePath = pipeNet.getNetData(pipePos, pipeSideToSource);
            if (routePath == null) continue;

            ComputationPort target = routePath.getComputationPort(level);
            if (target instanceof ComputationPortTrait targetTrait) {
                return targetTrait;
            }
        }
        return null;
    }

    private void unionNetworkSwitchPorts(Map<ComputationPortTrait, ComputationPortTrait> parent) {
        Map<NetworkSwitchMachine, ComputationPortTrait> firstPortBySwitch = new HashMap<>();
        for (ComputationPortTrait port : ports) {
            if (!(port.getMachine() instanceof IMultiPart part) || !part.isFormed()) continue;
            for (IMultiController controller : part.getControllers()) {
                if (controller instanceof NetworkSwitchMachine networkSwitch && isNetworkSwitchUsable(networkSwitch)) {
                    ComputationPortTrait first = firstPortBySwitch.putIfAbsent(networkSwitch, port);
                    if (first != null) {
                        union(parent, first, port);
                    }
                }
            }
        }
    }

    private boolean isNetworkSwitchUsable(NetworkSwitchMachine networkSwitch) {
        return networkSwitch.isFormed() && networkSwitch.getWorkLogic().isWorking();
    }

    private boolean isNetworkSwitchPort(ComputationPortTrait port) {
        if (!(port.getMachine() instanceof IMultiPart part) || !part.isFormed()) return false;
        return part.getControllers().stream().anyMatch(NetworkSwitchMachine.class::isInstance);
    }

    private boolean canUseComputationRoute(ComputationPortTrait first, ComputationPortTrait second) {
        boolean firstSwitch = isNetworkSwitchPort(first);
        boolean secondSwitch = isNetworkSwitchPort(second);
        if (!firstSwitch && !secondSwitch) return true;
        if (firstSwitch && !canProducerBridge(second)) return false;
        return !secondSwitch || canProducerBridge(first);
    }

    private boolean canProducerBridge(ComputationPortTrait port) {
        return port.getComputationProducer()
                .map(ComputationProducer::canBridgeComputation)
                .orElse(true);
    }

    private ComputationPortTrait find(Map<ComputationPortTrait, ComputationPortTrait> parent,
                                      ComputationPortTrait port) {
        ComputationPortTrait current = parent.get(port);
        if (current == port) return port;
        ComputationPortTrait root = find(parent, current);
        parent.put(port, root);
        return root;
    }

    private void union(Map<ComputationPortTrait, ComputationPortTrait> parent, ComputationPortTrait first,
                       ComputationPortTrait second) {
        ComputationPortTrait firstRoot = find(parent, first);
        ComputationPortTrait secondRoot = find(parent, second);
        if (firstRoot != secondRoot) {
            parent.put(secondRoot, firstRoot);
        }
    }

    private static class ComputationNetwork {

        private final ComputationSolver solver = new ComputationSolver();
        private final List<ComputationPortTrait> nodes = new ArrayList<>();
        private final Map<ComputationConsumer, Integer> lastAllocations = new HashMap<>();
        private final Map<ComputationConsumer, Integer> reservedCWUt = new HashMap<>();
        private final Set<ComputationConsumer> consumers = new HashSet<>();
        private int lastTotalOfferedCWUt;
        private int lastAllocatedCWUt;
        private int lastSpareCWUt;

        private void replaceNodes(List<ComputationPortTrait> newNodes) {
            nodes.clear();
            consumers.clear();
            nodes.addAll(newNodes);
            for(var node: newNodes) {
                node.getComputationConsumer().ifPresent(consumers::add);
            }
        }

        private void solve() {
            ComputationSolver.Result result = solver.solve(nodes);
            notifyChangedConsumers(result);
            lastAllocations.clear();
            lastAllocations.putAll(result.allocations());
            lastTotalOfferedCWUt = result.totalOfferedCWUt();
            lastAllocatedCWUt = result.allocatedCWUt();
            lastSpareCWUt = result.spareCWUt();
            reservedCWUt.clear();
        }

        private int getAvailableCWUt(ComputationConsumer consumer) {
            int allocated = lastAllocations.getOrDefault(consumer, 0);
            int reserved = reservedCWUt.entrySet().stream()
                    .filter(entry -> entry.getKey() != consumer)
                    .mapToInt(Map.Entry::getValue).sum();
            return Math.max(0, allocated + lastSpareCWUt - reserved);
        }

        private boolean reserveDemand(ComputationConsumer consumer, int requestedCWUt) {
            if (requestedCWUt <= 0) return true;
            if (getAvailableCWUt(consumer) < requestedCWUt) return false;
            reservedCWUt.merge(consumer, requestedCWUt, Integer::sum);
            return true;
        }

        private boolean contains(ComputationPortTrait trait) {
            return nodes.contains(trait);
        }

        private boolean contains(ComputationConsumer consumer) {
            return consumers.contains(consumer);
        }

        private void notifyChangedConsumers(ComputationSolver.Result result) {
            Map<ComputationConsumer, Integer> allocations = result.allocations();
            if (result.totalOfferedCWUt() > lastTotalOfferedCWUt || result.spareCWUt() > lastSpareCWUt) {
                for (ComputationConsumer consumer : allocations.keySet()) {
                    consumer.onComputationChanged();
                }
                return;
            }
            Set<ComputationConsumer> consumers = new HashSet<>(lastAllocations.keySet());
            consumers.addAll(allocations.keySet());
            for (ComputationConsumer consumer : consumers) {
                int previous = lastAllocations.getOrDefault(consumer, 0);
                int current = allocations.getOrDefault(consumer, 0);
                if (previous != current) {
                    consumer.onComputationChanged();
                }
            }
        }
    }
}
