package com.gregtechceu.gtceu.integration.ae2.util;

import appeng.api.stacks.AEKey;
import com.lowdragmc.lowdraglib.syncdata.IContentChangeAware;
import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class StackCounter implements ITagSerializable<ListTag>, IContentChangeAware, Iterable<Object2LongMap.Entry<AEKey>> {

    private final Object2LongMap<AEKey> records = new Object2LongOpenHashMap<>(); //TODO trim periodically or not

    @Nullable
    @Getter @Setter
    private Runnable onContentsChanged;

    public long get(AEKey key) {
        return records.getOrDefault(key, 0);
    }

    public long add(AEKey key, long amount) {
        if (amount != 0) {
            long oldValue = records.getOrDefault(key, 0);
            long changeValue = oldValue != 0 ? Math.min(Long.MAX_VALUE - oldValue, amount) : amount;
            long newValue = oldValue + changeValue;
            if (newValue > 0) {
                records.put(key, newValue);
                return changeValue;
            } else {
                return -oldValue;
            }
        } else {
            return 0;
        }
    }

    public void set(AEKey key, long amount) {
        if (amount <= 0) {
            records.removeLong(key);
        } else {
            records.put(key, amount);
        }
    }

    public int size() {
        var size = 0;
        for (var value : records.values()) {
            if (value > 0) {
                size++;
            }
        }

        return size;
    }

    public boolean isEmpty() {
        for (var value : records.values()) {
            if (value > 0) {
                return false;
            }
        }

        return true;
    }

    public void clear() {
        records.clear();
    }

    public void onChanged() {
        if (onContentsChanged != null) {
            onContentsChanged.run();
        }
    }

    @Override
    public ListTag serializeNBT() {
        var list = new ListTag();
        for (var entry : records.object2LongEntrySet()) {
            var tag = new CompoundTag();
            tag.put("key", entry.getKey().toTagGeneric());
            tag.putLong("value", entry.getLongValue());
            list.add(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(ListTag tags) {
        for (int i = 0; i < tags.size(); i++) {
            var tag = tags.getCompound(i);
            var key = AEKey.fromTagGeneric(tag.getCompound("key"));
            long value = tag.getLong("value");
            records.put(key, value);
        }
    }

    @NotNull
    @Override
    public Iterator<Object2LongMap.Entry<AEKey>> iterator() {
        return records.object2LongEntrySet().iterator();
    }
}
