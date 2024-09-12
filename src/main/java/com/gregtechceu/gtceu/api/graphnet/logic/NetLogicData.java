package com.gregtechceu.gtceu.api.graphnet.logic;

import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringRepresentable;

import com.jozufozu.flywheel.util.WeakHashSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private final Object2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> logicEntrySet;

    private final WeakHashSet<ILogicDataListener> listeners = new WeakHashSet<>();

    public NetLogicData() {
        logicEntrySet = new Object2ObjectOpenHashMap<>(4);
    }

    private NetLogicData(Object2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> logicEntrySet) {
        this.logicEntrySet = logicEntrySet;
    }

    /**
     * If the {@link NetLogicEntry#union(NetLogicEntry)} operation is not supported for this entry,
     * nothing happens if an entry is already present.
     */
    public NetLogicData mergeLogicEntry(NetLogicEntry<?, ?> entry) {
        NetLogicEntry<?, ?> current = logicEntrySet.get(entry.getType());
        if (current == null) return setLogicEntry(entry);

        if (entry.getClass().isInstance(current)) {
            entry = current.union(entry);
            if (entry == null) return this;
        }
        return setLogicEntry(entry);
    }

    public NetLogicData setLogicEntry(NetLogicEntry<?, ?> entry) {
        entry.registerToNetLogicData(this);
        logicEntrySet.put(entry.getType(), entry);
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
        return removeLogicEntry(key.getType());
    }

    public NetLogicData removeLogicEntry(@NotNull NetLogicType<?> key) {
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

    public boolean hasLogicEntry(@NotNull NetLogicType<?> type) {
        return logicEntrySet.containsKey(type);
    }

    public boolean hasLogicEntry(@NotNull NetLogicEntry<?, ?> key) {
        return logicEntrySet.containsKey(key.getType());
    }

    public <T extends NetLogicEntry<T, ?>> T getLogicEntryNullable(@NotNull NetLogicType<T> type) {
        return type.cast(logicEntrySet.get(type));
    }

    @NotNull
    public <T extends NetLogicEntry<T, ?>> T getLogicEntryDefaultable(@NotNull NetLogicType<T> type) {
        return type.cast(logicEntrySet.getOrDefault(type, type.getDefault()));
    }

    @Contract("null, null -> null; !null, _ -> new; _, !null -> new")
    public static @Nullable NetLogicData unionNullable(@Nullable NetLogicData sourceData,
                                                       @Nullable NetLogicData targetData) {
        if (sourceData == null && targetData == null) return null;
        return union(sourceData == null ? targetData : sourceData, sourceData == null ? null : targetData);
    }

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData sourceData, @Nullable NetLogicData targetData) {
        Object2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> newLogic = new Object2ObjectOpenHashMap<>(
                sourceData.logicEntrySet);
        if (targetData != null) {
            for (NetLogicType<?> key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(targetData.logicEntrySet.get(k)));
            }
            targetData.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    @Contract("_, _ -> new")
    public static @NotNull NetLogicData union(@NotNull NetLogicData first, @NotNull NetLogicData... others) {
        Object2ObjectOpenHashMap<NetLogicType<?>, NetLogicEntry<?, ?>> newLogic = new Object2ObjectOpenHashMap<>(
                first.logicEntrySet);
        for (NetLogicData other : others) {
            for (NetLogicType<?> key : newLogic.keySet()) {
                newLogic.computeIfPresent(key, (k, v) -> v.union(other.logicEntrySet.get(k)));
            }
            other.logicEntrySet.forEach((key, value) -> newLogic.computeIfAbsent(key, k -> value.union(null)));
        }
        return new NetLogicData(newLogic);
    }

    @Override
    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            CompoundTag tag = new CompoundTag();
            tag.putString("Type", entry.getType().getSerializedName());
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
            NetLogicType<?> type = NetLogicRegistry.getTypeNullable(tag.getString("Type"));
            if (type == null) continue;
            NetLogicEntry<?, ?> entry = this.logicEntrySet.get(type);
            if (entry == null) entry = type.getNew();
            entry.deserializeNBTNaive(tag.get("Tag"));
            this.logicEntrySet.put(type, entry);
        }
    }

    public void encode(FriendlyByteBuf buf) {
        int count = 0;
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            if (entry.shouldEncode()) count++;
        }
        buf.writeVarInt(count);
        for (NetLogicEntry<?, ?> entry : getEntries()) {
            if (entry.shouldEncode()) writeEntry(buf, entry, true);
        }
    }

    public void decode(FriendlyByteBuf buf) {
        this.logicEntrySet.clear();
        int entryCount = buf.readVarInt();
        for (int i = 0; i < entryCount; i++) {
            readEntry(buf);
        }
        this.logicEntrySet.trim();
    }

    public static void writeEntry(@NotNull FriendlyByteBuf buf, @NotNull NetLogicEntry<?, ?> entry, boolean fullChange) {
        buf.writeVarInt(NetLogicRegistry.getNetworkID(entry));
        buf.writeBoolean(fullChange);
        entry.encode(buf, fullChange);
    }

    /**
     * @return the net logic entry decoded to.
     */
    @Nullable
    public NetLogicEntry<?, ?> readEntry(@NotNull FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        boolean fullChange = buf.readBoolean();
        NetLogicType<?> type = NetLogicRegistry.getType(id);
        NetLogicEntry<?, ?> existing = this.getLogicEntryNullable(type);
        boolean add = false;
        if (existing == null) {
            // never partially decode into a new entry
            if (!fullChange) return null;
            existing = type.getNew();
            add = true;
        }
        try {
            existing.decode(buf, fullChange);
        } catch (Exception ignored) {
            NetLogicRegistry.throwDecodingError();
        }
        // make sure to add after decoding, so we don't notify listeners with an empty logic entry
        if (add) this.setLogicEntry(existing);
        return existing;
    }

    /**
     * Adds a listener to a weak set which is then notified for as long as it is not collected by the garbage collector.
     *
     * @param listener the listener.
     * @return the listener, for convenience when working with lambdas.
     */
    public ILogicDataListener addListener(ILogicDataListener listener) {
        this.listeners.add(listener);
        return listener;
    }

    @FunctionalInterface
    public interface ILogicDataListener {

        void markChanged(NetLogicEntry<?, ?> updatedEntry, boolean removed, boolean fullChange);
    }
}
