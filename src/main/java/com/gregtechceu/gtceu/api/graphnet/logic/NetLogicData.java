package com.gregtechceu.gtceu.api.graphnet.logic;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Note - since the internal map representation encodes keys using {@link StringRepresentable#getSerializedName()} on
 * logics,
 * making a logics class return two different names is a valid way to register multiple instances.
 */
public final class NetLogicData implements ITagSerializable<ListTag>, IContentChangeAware, INetLogicEntryListener {

    @Nullable
    @Getter
    @Setter
    private Runnable onContentsChanged = () -> {};

    // TODO caching logic on simple logics to reduce amount of reduntant creation?
    private final Object2ObjectOpenHashMap<String, NetLogicEntry<?, ?>> logicEntrySet;

    private final Set<LogicDataListener> listeners = new ObjectOpenHashSet<>();

    public NetLogicData() {
        logicEntrySet = new Object2ObjectOpenHashMap<>(4);
    }

    private NetLogicData(Object2ObjectOpenHashMap<String, NetLogicEntry<?, ?>> logicEntrySet) {
        this.logicEntrySet = logicEntrySet;
    }

    /**
     * If the {@link NetLogicEntry#union(NetLogicEntry)} operation is not supported for this entry,
     * nothing happens if an entry is already present.
     */
    public NetLogicData mergeLogicEntry(NetLogicEntry<?, ?> entry) {
        NetLogicEntry<?, ?> current = logicEntrySet.get(entry.getSerializedName());
        if (current == null) return setLogicEntry(entry);

        if (entry.getClass().isInstance(current)) {
            entry = current.union(entry);
            if (entry == null) return this;
        }
        return setLogicEntry(entry);
    }

    public NetLogicData setLogicEntry(NetLogicEntry<?, ?> entry) {
        entry.registerToNetLogicData(this);
        logicEntrySet.put(entry.getSerializedName(), entry);
        this.markLogicEntryAsUpdated(entry, true);
        return this;
    }

    /**
     * Returns all registered logic entries; this should be treated in read-only manner.
     */
    public ObjectCollection<NetLogicEntry<?, ?>> getEntries() {
        return logicEntrySet.values();
    }

    public void clearData() {
        logicEntrySet.clear();
        logicEntrySet.trim(4);
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    public NetLogicData removeLogicEntry(@NotNull NetLogicEntry<?, ?> key) {
        return removeLogicEntry(key.getSerializedName());
    }

    public NetLogicData removeLogicEntry(@NotNull String key) {
        NetLogicEntry<?, ?> entry = logicEntrySet.remove(key);
        if (entry != null) {
            entry.deregisterFromNetLogicData(this);
            this.listeners.forEach(l -> l.markChanged(entry, true, true));
            logicEntrySet.trim();
        }
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
        return this;
    }

    @Override
    public void markLogicEntryAsUpdated(NetLogicEntry<?, ?> entry, boolean fullChange) {
        this.listeners.forEach(l -> l.markChanged(entry, false, fullChange));
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    public boolean hasLogicEntry(@NotNull String key) {
        return logicEntrySet.containsKey(key);
    }

    public boolean hasLogicEntry(@NotNull NetLogicEntry<?, ?> key) {
        return logicEntrySet.containsKey(key.getSerializedName());
    }

    @Nullable
    public NetLogicEntry<?, ?> getLogicEntryNullable(@NotNull String key) {
        return logicEntrySet.get(key);
    }

    @Nullable
    public <T extends NetLogicEntry<?, ?>> T getLogicEntryNullable(@NotNull T key) {
        try {
            return (T) logicEntrySet.get(key.getSerializedName());
        } catch (ClassCastException ignored) {
            return null;
        }
    }

    @NotNull
    public <T extends NetLogicEntry<T, ?>> T getLogicEntryDefaultable(@NotNull T key) {
        try {
            T returnable = (T) logicEntrySet.get(key.getSerializedName());
            return returnable == null ? key : returnable;
        } catch (ClassCastException ignored) {
            return key;
        }
    }

    @Contract("null, null -> null; !null, _ -> new; _, !null -> new")
    public static @Nullable NetLogicData unionNullable(@Nullable NetLogicData sourceData,
                                                       @Nullable NetLogicData targetData) {
        if (sourceData == null && targetData == null) return null;
        return union(sourceData == null ? targetData : sourceData, sourceData == null ? null : targetData);
    }

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData sourceData, @Nullable NetLogicData targetData) {
        Object2ObjectOpenHashMap<String, NetLogicEntry<?, ?>> newLogic = new Object2ObjectOpenHashMap<>(
                sourceData.logicEntrySet);
        if (targetData != null) {
            for (String key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(targetData.logicEntrySet.get(k)));
            }
            targetData.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData first, @NotNull NetLogicData... others) {
        Object2ObjectOpenHashMap<String, NetLogicEntry<?, ?>> newLogic = new Object2ObjectOpenHashMap<>(
                first.logicEntrySet);
        for (NetLogicData other : others) {
            for (String key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(other.logicEntrySet.get(k)));
            }
            other.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    public void addListener(LogicDataListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Name", entry.getSerializedName());
            Tag nbt = entry.serializeNBT();
            if (nbt != null) tag.put("Tag", nbt);
            list.add(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundTag tag = nbt.getCompound(i);
            String key = tag.getString("Name");
            NetLogicEntry<?, ?> entry = this.logicEntrySet.get(key);
            if (entry == null) entry = NetLogicRegistry.getSupplierNotNull(key).get();
            if (entry == null) continue;
            entry.deserializeNBTNaive(tag.get("Tag"));
        }
    }

    // @Override
    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(getEntries().size());
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            if (entry.shouldEncode()) {
                buf.writeUtf(entry.getSerializedName());
                entry.encode(buf, true);
            } else {
                buf.writeUtf("");
            }
        }
    }

    // @Override
    public void decode(FriendlyByteBuf buf) {
        this.logicEntrySet.clear();
        int entryCount = buf.readVarInt();
        for (int i = 0; i < entryCount; i++) {
            String name = buf.readUtf(255);
            if (name.isEmpty()) continue;
            NetLogicEntry<?, ?> existing = NetLogicRegistry.getSupplierErroring(name).get();
            existing.registerToNetLogicData(this);
            existing.decode(buf);
            this.logicEntrySet.put(name, existing);
        }
        this.logicEntrySet.trim();
    }

    public LogicDataListener createListener(ILogicDataListener listener) {
        return new LogicDataListener(listener);
    }

    public final class LogicDataListener {

        private final ILogicDataListener listener;

        private LogicDataListener(ILogicDataListener listener) {
            this.listener = listener;
        }

        private void markChanged(NetLogicEntry<?, ?> updatedEntry, boolean removed, boolean fullChange) {
            this.listener.markChanged(updatedEntry, removed, fullChange);
        }

        // TODO would a weak set be better?
        public void invalidate() {
            listeners.remove(this);
        }
    }

    @FunctionalInterface
    public interface ILogicDataListener {

        void markChanged(NetLogicEntry<?, ?> updatedEntry, boolean removed, boolean fullChange);
    }
}
