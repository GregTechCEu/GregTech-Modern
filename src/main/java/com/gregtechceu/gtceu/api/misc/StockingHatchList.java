package com.gregtechceu.gtceu.api.misc;

import appeng.api.stacks.AEKey;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

// List of AEStacks, always sorted largest to smallest.
public class StockingHatchList implements Iterable<StockingHatchList.AEStack> {

    private final List<AEStack> list;
    private final int capacity;

    public StockingHatchList(int capacity) {
        this.capacity = capacity;
        this.list = new ArrayList<>(capacity);
    }

    public boolean insert(AEKey key, long amount) {
        for (int i = 0; i < list.size(); i++) {
            AEStack stack = list.get(i);
            if (!key.equals(stack.key)) {
                continue;
            }
            if (amount > stack.amount) {
                // stack grew, resort the area before it
                stack.amount = amount;
                if (i > 0 && list.get(i - 1).amount < amount) {
                    for (int j = 0; j < i; j++) {
                        if (amount > list.get(j).amount) {
                            list.add(j, stack);
                            list.remove(i + 1); // remove old stack which is now shifted
                            return true;
                        }
                    }
                }
                // didn't need to move, already updated in place
                return true;
            } else if (amount < stack.amount) {
                // stack shrunk, resort the area after it
                stack.amount = amount;
                if (i + 1 < list.size() && list.get(i + 1).amount > amount) {
                    for (int j = i + 1; j < list.size(); j++) {
                        if (amount > list.get(j).amount) {
                            list.add(j, stack);
                            list.remove(i); // remove old stack
                            return true;
                        }
                    }
                }
                // didn't need to move, already updated in place
                return true;
            }
            // no change
            return false;
        }
        // not enough space in list
        if (list.size() >= capacity) {
            return false;
        }
        // unseen item
        for (int i = 0; i < list.size(); i++) {
            AEStack stack = list.get(i);
            if (amount > stack.amount) {
                list.add(i, new AEStack(key, amount));
                return true;
            }
        }
        list.add(new AEStack(key, amount));
        return true;
    }

    public boolean remove(AEKey key) {
        for (int i = 0; i < list.size(); i++) {
            AEStack stack = list.get(i);
            if (stack.key.equals(key)) {
                list.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean contains(AEKey what) {
        for (var stack : list) {
            if (stack.getKey().equals(what)) return true;
        }
        return false;
    }

    @Override
    public @NotNull Iterator<AEStack> iterator() {
        return list.iterator();
    }

    public int size() {
        return list.size();
    }

    public void clear() {
        list.clear();
    }

    public StockingHatchList.AEStack get(int index) {
        return list.get(index);
    }

    public static class AEStack {

        @Getter
        @Setter
        public @NotNull AEKey key;
        @Getter
        @Setter
        public long amount;

        public AEStack(@NotNull AEKey key, long amount) {
            this.key = key;
            this.amount = amount;
        }
    }
}
