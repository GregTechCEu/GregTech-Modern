package com.gregtechceu.gtceu.integration.ae2.mui;

import com.gregtechceu.gtceu.api.mui.value.sync.ValueSyncHandler;
import com.gregtechceu.gtceu.integration.ae2.utils.KeyStorage;

import net.minecraft.network.FriendlyByteBuf;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Syncs AE2 KeyStorage contents as a sorted List of GenericStack.
 * <p>
 * The list is maintained in descending amount order. When only amounts change (no keys
 * added/removed), the list is mutated in place without triggering onValueChanged() —
 * this avoids DynamicLinkedSyncHandler rebuilding the widget tree. Widgets read from
 * the list by index each frame, so they pick up the new amounts automatically.
 * <p>
 * onValueChanged() only fires on structural changes (keys added or removed), which
 * triggers DynamicLinkedSyncHandler to rebuild the Grid with the correct widget count.
 */
@SuppressWarnings("unchecked")
public class AEKeyStorageSyncHandler extends ValueSyncHandler<List<GenericStack>> {

    private static final Comparator<GenericStack> BY_AMOUNT_DESC = (a, b) -> Long.compare(b.amount(), a.amount());

    private final KeyStorage serverStorage;
    private final Object2LongMap<AEKey> cached = new Object2LongOpenHashMap<>();
    private List<GenericStack> value = Collections.emptyList();

    public AEKeyStorageSyncHandler(KeyStorage storage) {
        this.serverStorage = storage;
    }

    @Override
    public List<GenericStack> getValue() {
        return value;
    }

    @Override
    public void setValue(List<GenericStack> value, boolean setSource, boolean sync) {
        this.value = value;
        onValueChanged();
        if (sync) sync();
    }

    @Override
    public boolean updateCacheFromSource(boolean isFirstSync) {
        if (!isFirstSync && !hasChanged()) return false;

        boolean keysChanged = isFirstSync || keysChanged();

        cached.clear();
        cached.putAll(serverStorage.storage);

        if (keysChanged) {
            rebuildList();
            onValueChanged();
        } else {
            updateAmountsInPlace();
        }
        sync();
        return true;
    }

    private boolean hasChanged() {
        if (cached.size() != serverStorage.storage.size()) return true;
        for (var entry : serverStorage.storage.object2LongEntrySet()) {
            if (cached.getOrDefault(entry.getKey(), 0) != entry.getLongValue()) return true;
        }
        return false;
    }

    private boolean keysChanged() {
        if (cached.size() != serverStorage.storage.size()) return true;
        Set<AEKey> currentKeys = serverStorage.storage.keySet();
        for (AEKey key : cached.keySet()) {
            if (!currentKeys.contains(key)) return true;
        }
        return false;
    }

    private void rebuildList() {
        List<GenericStack> list = new ArrayList<>();
        for (var entry : serverStorage.storage.object2LongEntrySet()) {
            list.add(new GenericStack(entry.getKey(), entry.getLongValue()));
        }
        list.sort(BY_AMOUNT_DESC);
        this.value = list;
    }

    private void updateAmountsInPlace() {
        // Replace entries with updated amounts, re-sort
        for (int i = 0; i < value.size(); i++) {
            GenericStack old = value.get(i);
            long newAmount = serverStorage.storage.getOrDefault(old.what(), 0);
            if (old.amount() != newAmount) {
                value.set(i, new GenericStack(old.what(), newAmount));
            }
        }
        value.sort(BY_AMOUNT_DESC);
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeVarInt(value.size());
        for (GenericStack stack : value) {
            GenericStack.writeBuffer(stack, buffer);
        }
    }

    @Override
    public void read(FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();

        List<GenericStack> incoming = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            GenericStack stack = GenericStack.readBuffer(buffer);
            if (stack != null) incoming.add(stack);
        }
        incoming.sort(BY_AMOUNT_DESC);

        boolean structural = structurallyDifferent(incoming);
        this.value = incoming;

        if (structural) {
            onValueChanged();
        }
    }

    private boolean structurallyDifferent(List<GenericStack> incoming) {
        if (value.size() != incoming.size()) return true;
        Set<AEKey> oldKeys = value.stream().map(GenericStack::what).collect(Collectors.toSet());
        Set<AEKey> newKeys = incoming.stream().map(GenericStack::what).collect(Collectors.toSet());
        return !oldKeys.equals(newKeys);
    }

    @Override
    public void notifyUpdate() {
        updateCacheFromSource(true);
    }

    @Override
    public Class<List<GenericStack>> getValueType() {
        return (Class<List<GenericStack>>) (Object) List.class;
    }
}
