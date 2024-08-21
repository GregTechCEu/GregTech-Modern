package com.gregtechceu.gtceu.api.graphnet.predicate;

import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;

import com.lowdragmc.lowdraglib.syncdata.ITagSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.StringRepresentable;
import net.minecraftforge.common.util.INBTSerializable;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Note - since the internal map representation encodes keys using {@link StringRepresentable#getSerializedName()} on
 * predicates,
 * making a predicate class return two different names is a valid way to register multiple instances.
 */
public final class EdgePredicateHandler implements ITagSerializable<ListTag>, Predicate<IPredicateTestObject> {

    private final Map<String, EdgePredicate<?, ?>> predicateSet;

    public EdgePredicateHandler() {
        predicateSet = new Object2ObjectOpenHashMap<>();
    }

    /**
     * If the {@link EdgePredicate#union(EdgePredicate)} operation is not supported for this predicate,
     * nothing happens if a predicate is already present.
     */
    public EdgePredicateHandler mergePredicate(@NotNull EdgePredicate<?, ?> predicate) {
        EdgePredicate<?, ?> current = predicateSet.get(predicate.getSerializedName());
        if (current == null) return setPredicate(predicate);

        if (predicate.getClass().isInstance(current)) {
            predicate = current.union(predicate);
            if (predicate == null) return this;
        }
        return setPredicate(predicate);
    }

    /**
     * Do not modify the returned value
     */
    public Map<String, EdgePredicate<?, ?>> getPredicateSet() {
        return predicateSet;
    }

    public EdgePredicateHandler setPredicate(@NotNull EdgePredicate<?, ?> predicate) {
        predicateSet.put(predicate.getSerializedName(), predicate);
        return this;
    }

    public EdgePredicateHandler removePredicate(@NotNull EdgePredicate<?, ?> predicate) {
        return removePredicate(predicate.getSerializedName());
    }

    public EdgePredicateHandler removePredicate(String key) {
        predicateSet.remove(key);
        return this;
    }

    public boolean hasPredicate(@NotNull EdgePredicate<?, ?> predicate) {
        return predicateSet.containsKey(predicate.getSerializedName());
    }

    public boolean hasPredicate(String key) {
        return predicateSet.containsKey(key);
    }

    public void clearPredicates() {
        this.predicateSet.clear();
    }

    public boolean shouldIgnore() {
        return predicateSet.isEmpty();
    }

    @Override
    public boolean test(IPredicateTestObject iPredicateTestObject) {
        if (shouldIgnore()) return true;
        boolean result = false;
        for (EdgePredicate<?, ?> predicate : predicateSet.values()) {
            // TODO predicate 'categories' or 'affinities' that determine order of operations with and-y and or-y
            // behavior?
            boolean test = predicate.test(iPredicateTestObject);
            if (predicate.andy() && !test) return false;
            else result |= test;
        }
        return result;
    }

    @Override
    public ListTag serializeNBT() {
        ListTag list = new ListTag();
        for (EdgePredicate<?, ?> entry : predicateSet.values()) {
            CompoundTag tag = new CompoundTag();
            tag.put("Tag", entry.serializeNBT());
            tag.putString("Name", entry.getSerializedName());
            list.add(tag);
        }
        return list;
    }

    @Override
    public void deserializeNBT(ListTag nbt) {
        for (int i = 0; i < nbt.size(); i++) {
            CompoundTag tag = nbt.getCompound(i);
            String key = tag.getString("Name");
            EdgePredicate<?, ?> entry = this.predicateSet.get(key);
            if (entry == null) entry = NetPredicateRegistry.getSupplierNotNull(key).get();
            if (entry == null) continue;
            entry.deserializeNBTNaive(tag.get("Tag"));
        }
    }
}
