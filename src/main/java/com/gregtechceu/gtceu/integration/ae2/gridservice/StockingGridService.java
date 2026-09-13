package com.gregtechceu.gtceu.integration.ae2.gridservice;

import com.gregtechceu.gtceu.integration.ae2.machine.feature.multiblock.IMEStockingPart;

import net.minecraft.nbt.CompoundTag;

import appeng.api.config.Actionable;
import appeng.api.networking.GridServices;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridServiceProvider;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StockingGridService implements IStockingService, IGridServiceProvider {

    public static void register() {
        GridServices.register(IStockingService.class, StockingGridService.class);
    }

    private final IGrid grid;
    private final Set<IMEStockingPart> parts = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<AEKey, Set<IMEStockingPart>> keyToParts = new HashMap<>();
    private final Map<IMEStockingPart, Set<AEKey>> partKeys = new IdentityHashMap<>();
    private final Object2LongMap<AEKey> lastAmounts = new Object2LongOpenHashMap<>();
    private final Set<IMEStockingPart> pendingRefresh = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<IMEStockingPart> pendingAutoPull = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<IMEStockingPart, Set<AEKey>> pendingStockUpdates = new IdentityHashMap<>();
    private final Set<IMEStockingPart> pendingImmediate = Collections.newSetFromMap(new IdentityHashMap<>());

    public StockingGridService(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void addNode(IGridNode node, @Nullable CompoundTag savedData) {
        if (node.getOwner() instanceof IMEStockingPart part) {
            parts.add(part);
            pendingRefresh.add(part);
            if (part.isAutoPull()) {
                pendingAutoPull.add(part);
            }
        }
    }

    @Override
    public void removeNode(IGridNode node) {
        if (node.getOwner() instanceof IMEStockingPart part) {
            unregister(part);
        }
    }

    @Override
    public void markForRefresh(IMEStockingPart part) {
        if (parts.contains(part)) {
            pendingRefresh.add(part);
        }
    }

    @Override
    public void markForAutoPull(IMEStockingPart part) {
        if (parts.contains(part)) {
            pendingAutoPull.add(part);
        }
    }

    private void unregister(IMEStockingPart part) {
        parts.remove(part);
        pendingRefresh.remove(part);
        pendingAutoPull.remove(part);
        pendingStockUpdates.remove(part);
        pendingImmediate.remove(part);

        Set<AEKey> keys = partKeys.remove(part);
        if (keys != null) {
            for (AEKey key : keys) {
                removeInterest(key, part);
            }
        }
    }

    private void removeInterest(AEKey key, IMEStockingPart part) {
        Set<IMEStockingPart> interested = keyToParts.get(key);
        if (interested != null) {
            interested.remove(part);
            if (interested.isEmpty()) {
                keyToParts.remove(key);
                lastAmounts.removeLong(key);
            }
        }
    }

    @Override
    public void onServerStartTick() {
        if (parts.isEmpty()) {
            return;
        }

        KeyCounter cached = grid.getStorageService().getCachedInventory();

        processAutoPull(cached);
        processPendingRegistrations(cached);
        detectStockChanges(cached);
        applyStockUpdates(cached);
    }

    private void processAutoPull(KeyCounter cached) {
        List<IMEStockingPart> duePulls = new ArrayList<>();

        for (IMEStockingPart part : parts) {
            if (part.isAutoPull() && part.isOnline() &&
                    (part.isCycleDue() || pendingAutoPull.contains(part))) {
                duePulls.add(part);
            }
        }

        pendingAutoPull.clear();

        MEStorage storage = grid.getStorageService().getInventory();
        var ranked = rankByAmountDescending(cached);
        Object2LongMap<AEKey> extractableCache = new Object2LongOpenHashMap<>();

        for (IMEStockingPart part : duePulls) {
            refreshAutoPull(part, ranked, storage, extractableCache);
        }
    }

    private void processPendingRegistrations(KeyCounter cached) {
        if (pendingRefresh.isEmpty()) {
            return;
        }

        List<IMEStockingPart> refreshing = new ArrayList<>(pendingRefresh);
        pendingRefresh.clear();

        for (IMEStockingPart part : refreshing) {
            if (!parts.contains(part)) {
                continue;
            }

            refreshRegistration(part, cached);
        }
    }

    private void detectStockChanges(KeyCounter cached) {
        for (var entry : keyToParts.entrySet()) {
            AEKey key = entry.getKey();
            long amount = cached.get(key);

            if (lastAmounts.containsKey(key) && lastAmounts.getLong(key) == amount) {
                continue;
            }

            lastAmounts.put(key, amount);

            for (IMEStockingPart part : entry.getValue()) {
                pendingStockUpdates.computeIfAbsent(part, ignored -> new HashSet<>()).add(key);
            }
        }
    }

    private void applyStockUpdates(KeyCounter cached) {
        if (pendingStockUpdates.isEmpty()) {
            return;
        }

        MEStorage storage = null;
        var realAmounts = new Object2LongOpenHashMap<>();

        var iterator = pendingStockUpdates.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            IMEStockingPart part = entry.getKey();

            if (!parts.contains(part)) {
                iterator.remove();
                pendingImmediate.remove(part);
                continue;
            }

            if (part.isOnline() && !part.isCycleDue() && !pendingImmediate.contains(part)) {
                continue;
            }

            Set<AEKey> keys = entry.getValue();
            iterator.remove();
            pendingImmediate.remove(part);

            if (keys.isEmpty()) {
                continue;
            }

            if (storage == null) {
                storage = grid.getStorageService().getInventory();
            }

            Object2LongMap<AEKey> changes = new Object2LongOpenHashMap<>(keys.size());
            for (AEKey key : keys) {
                long realAmount;

                if (realAmounts.containsKey(key)) {
                    realAmount = realAmounts.getLong(key);
                } else {
                    realAmount = storage.extract(key, cached.get(key), Actionable.SIMULATE, part.getActionSource());
                    realAmounts.put(key, realAmount);
                }

                changes.put(key, realAmount);
            }

            if (part.applyStockSilent(changes)) {
                part.notifyStockChanged();
            }
        }
    }

    private void refreshRegistration(IMEStockingPart part, KeyCounter cached) {
        Set<AEKey> newKeys = new HashSet<>(part.getStockingKeys());
        Set<AEKey> oldKeys = partKeys.getOrDefault(part, Collections.emptySet());
        Set<AEKey> queuedKeys = pendingStockUpdates.get(part);

        for (AEKey key : oldKeys) {
            if (!newKeys.contains(key)) {
                removeInterest(key, part);
                if (queuedKeys != null) {
                    queuedKeys.remove(key);
                }
            }
        }

        if (queuedKeys != null && queuedKeys.isEmpty()) {
            pendingStockUpdates.remove(part);
        }

        for (AEKey key : newKeys) {
            keyToParts.computeIfAbsent(key, ignored -> Collections.newSetFromMap(new IdentityHashMap<>())).add(part);

            if (!lastAmounts.containsKey(key)) {
                lastAmounts.put(key, cached.get(key));
            }

            pendingStockUpdates.computeIfAbsent(part, ignored -> new HashSet<>()).add(key);
        }

        if (newKeys.isEmpty()) {
            partKeys.remove(part);
        } else {
            partKeys.put(part, newKeys);
            pendingImmediate.add(part);
        }
    }

    private List<Object2LongMap.Entry<AEKey>> rankByAmountDescending(KeyCounter cached) {
        List<Object2LongMap.Entry<AEKey>> ranked = new ArrayList<>();

        for (Object2LongMap.Entry<AEKey> entry : cached) {
            if (entry.getLongValue() > 0) {
                ranked.add(entry);
            }
        }

        ranked.sort((a, b) -> Long.compare(b.getLongValue(), a.getLongValue()));
        return ranked;
    }

    private void refreshAutoPull(IMEStockingPart part, List<Object2LongMap.Entry<AEKey>> ranked, MEStorage storage,
                                 Object2LongMap<AEKey> extractableCache) {
        int configSize = part.getSlotList().getConfigurableSlots();
        int minStackSize = part.getMinStackSize();
        List<GenericStack> selection = new ArrayList<>(configSize);

        for (var entry : ranked) {
            if (selection.size() >= configSize) {
                break;
            }

            long amount = entry.getLongValue();
            if (amount < minStackSize) {
                break;
            }

            AEKey what = entry.getKey();
            if (!part.isAutoPullValid(what, amount)) {
                continue;
            }

            long extractable;
            if (extractableCache.containsKey(what)) {
                extractable = extractableCache.getLong(what);
            } else {
                extractable = storage.extract(what, amount, Actionable.SIMULATE, part.getActionSource());
                extractableCache.put(what, extractable);
            }

            if (extractable < minStackSize) {
                continue;
            }

            selection.add(new GenericStack(what, extractable));
        }

        part.applyAutoPull(selection);
    }
}
