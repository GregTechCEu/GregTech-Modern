package com.gregtechceu.gtceu.common.cover.filter;

import com.gregtechceu.gtceu.api.graphnet.predicate.test.IPredicateTestObject;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public final class MergabilityInfo<T extends IPredicateTestObject> {

    private final Object2ObjectOpenHashMap<T, Merge> mergeMap = new Object2ObjectOpenHashMap<>();

    public void add(int handlerSlot, T testObject, int count) {
        mergeMap.compute(testObject, (k, v) -> {
            if (v == null) v = new Merge(k);
            v.count += count;
            v.handlerSlots.add(handlerSlot);
            return v;
        });
    }

    public Merge getLargestMerge() {
        return mergeMap.values().stream().max(Comparator.comparingInt(Merge::getCount)).orElse(null);
    }

    public @NotNull List<Merge> getNonLargestMerges(@Nullable Merge largestMerge) {
        List<Merge> merges = new ObjectArrayList<>(mergeMap.values());
        merges.remove(largestMerge == null ? getLargestMerge() : largestMerge);
        return merges;
    }

    public final class Merge {

        private final T testObject;

        private int count = 0;
        private final IntList handlerSlots = new IntArrayList();

        public Merge(T testObject) {
            this.testObject = testObject;
        }

        public int getCount() {
            return count;
        }

        public T getTestObject() {
            return testObject;
        }

        public IntList getHandlerSlots() {
            return handlerSlots;
        }
    }
}
