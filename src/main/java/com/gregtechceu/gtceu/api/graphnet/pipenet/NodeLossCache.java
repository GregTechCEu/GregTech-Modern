package com.gregtechceu.gtceu.api.graphnet.pipenet;

import com.gregtechceu.gtceu.api.graphnet.edge.SimulatorKey;
import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import com.gregtechceu.gtceu.api.graphnet.traverse.ITraverseData;
import com.gregtechceu.gtceu.utils.TaskScheduler;
import com.gregtechceu.gtceu.utils.function.Task;

import net.minecraft.world.level.Level;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

public class NodeLossCache implements Task {

    private static final WeakHashMap<WorldPipeNet, NodeLossCache> CACHE = new WeakHashMap<>();

    public static void registerLossResult(Key key, NodeLossResult result) {
        NodeLossCache existing = CACHE.get(key.node().getNet());
        if (existing == null) {
            existing = new NodeLossCache(key.node().getNet().getLevel());
            CACHE.put(key.node().getNet(), existing);
        }
        existing.registerResult(key, result);
    }

    public static @Nullable NodeLossResult getLossResult(Key key) {
        NodeLossCache existing = CACHE.get(key.node().getNet());
        if (existing == null) {
            existing = new NodeLossCache(key.node().getNet().getLevel());
            CACHE.put(key.node().getNet(), existing);
        }
        return existing.getResult(key);
    }

    private final Map<Key, NodeLossResult> cache = new Object2ObjectOpenHashMap<>();

    private NodeLossCache(Level world) {
        TaskScheduler.scheduleTask(world, TaskScheduler.weakTask(this));
    }

    public void registerResult(Key key, NodeLossResult result) {
        cache.put(key, result);
    }

    public @Nullable NodeLossResult getResult(Key key) {
        return cache.get(key);
    }

    @Override
    public boolean run() {
        if (cache.isEmpty()) return true;
        for (var result : cache.entrySet()) {
            result.getValue().triggerPostAction(result.getKey().node());
        }
        cache.clear();
        return true;
    }

    public static Key key(WorldPipeNetNode node, IPredicateTestObject testObject, SimulatorKey simulator) {
        return new Key(node, testObject, simulator);
    }

    public static Key key(WorldPipeNetNode node, ITraverseData<?, ?> data) {
        return new Key(node, data.getTestObject(), data.getSimulatorKey());
    }

    public record Key(WorldPipeNetNode node, IPredicateTestObject testObject, SimulatorKey simulator) {}
}
