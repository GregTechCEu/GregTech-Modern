package com.gregtechceu.gtceu.api.multiblock.pattern;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection;

import com.google.common.base.Preconditions;

import java.util.*;

public class BasicSliceStrategy extends SliceStrategy {

    protected static class MultiblockSlice {

        public int minRepeats;
        public int maxRepeats;
        public int startInclusive;
        public int endExclusive;
        public int actualRepeats;

        public MultiblockSlice(int minRepeats, int maxRepeats, int startInclusive, int endExclusive,
                               int actualRepeats) {
            this.minRepeats = minRepeats;
            this.maxRepeats = maxRepeats;
            this.startInclusive = startInclusive;
            this.endExclusive = endExclusive;
            this.actualRepeats = actualRepeats;
        }

        @Override
        public String toString() {
            return String.format("[min %s, max %s, startInc %s, endExc %s, actual %s]", minRepeats, maxRepeats,
                    startInclusive, endExclusive, actualRepeats);
        }
    }

    protected final List<MultiblockSlice> multiblockSlices = new ArrayList<>();
    protected final List<PatternSlice> slices = new ArrayList<>();
    protected final int[] result = new int[2];

    @Override
    public boolean check(PatternState state, boolean flip) {
        int offset = 0;
        for (var multiSlice : multiblockSlices) {
            int result = checkMultiSlice(state, multiSlice, offset, flip);
            if (result == -1) return false;
            offset += result;
        }
        return true;
    }

    public int getMultiSliceRepeats(int index) {
        return multiblockSlices.get(index).actualRepeats;
    }

    protected int checkMultiSlice(PatternState state, MultiblockSlice multiblockSlice, int offset, boolean flip) {
        int sliceOffset = 0;
        int temp = 0;
        for (int i = 1; i <= multiblockSlice.maxRepeats; i++) {
            for (int j = multiblockSlice.startInclusive; j < multiblockSlice.endExclusive; j++) {
                int res = checkRepeatSlice(state, j, offset + temp, flip);
                if (res == -1) {
                    if (i <= multiblockSlice.minRepeats) return -1;
                    multiblockSlice.actualRepeats = i - 1;
                    return sliceOffset;
                }
                temp += res;
            }
            sliceOffset = temp;
        }

        multiblockSlice.actualRepeats = multiblockSlice.maxRepeats;
        return sliceOffset;
    }

    protected int checkRepeatSlice(PatternState state, int index, int offset, boolean flip) {
        PatternSlice slice = slices.get(index);
        for (int i = 1; i <= slice.maxRepeats; i++) {
            boolean res = checkSlice(state, index, offset + i - 1, flip);
            if (!res) {
                if (i <= slice.minRepeats) return -1;

                return slices.get(index).actualRepeats = i - 1;
            }
        }
        return slices.get(index).actualRepeats = slice.maxRepeats;
    }

    @Override
    protected void finish(int[] dimensions, RelativeDirection[] directions, List<PatternSlice> slices) {
        super.finish(dimensions, directions, slices);

        this.slices.addAll(slices);

        BitSet covered = new BitSet(slices.size());
        int sum = 0;
        for (var arr : multiblockSlices) {
            covered.set(arr.startInclusive, arr.endExclusive);
            sum += arr.endExclusive - arr.startInclusive;
        }

        if (sum != covered.cardinality()) {
            GTCEu.LOGGER.error("Overlapping multiblock slices. " +
                    "Multiblock has {} slices total but only {} distinct slices.", sum, covered.cardinality());
            multiSliceError();
        }
        if (sum > slices.size()) {
            GTCEu.LOGGER.error("multiSlices out of bounds. {} slices total but {} slices in multiSlices",
                    slices.size(), sum);
            multiSliceError();
        }

        int i = covered.nextClearBit(0);
        while ((i = covered.nextClearBit(i)) < slices.size()) {
            multiblockSlices.add(new MultiblockSlice(1, 1, i, i + 1, -1));
            covered.set(i);
        }

        multiblockSlices.sort(Comparator.comparingInt(a -> a.startInclusive));
    }

    protected void multiSliceError() {
        GTCEu.LOGGER.error(
                "multiSlices in the pattern, formatted as [minRepeats, maxRepeats, startInclusive, endExclusive, actualRepeats]");
        for (var arr : multiblockSlices) {
            GTCEu.LOGGER.error(arr.toString());
        }
        throw new IllegalStateException("Illegal multiSlices, see log above.");
    }

    public BasicSliceStrategy multiSlice(int min, int max, int from, int to) {
        Preconditions.checkArgument(max >= min, "max: %s is less than min: %s", max, min);
        Preconditions.checkArgument(from >= 0, "from argument is negative: %s", from);
        Preconditions.checkArgument(to > 0, "to argument is not positive: %s", to);
        multiblockSlices.add(new MultiblockSlice(min, max, from, to, -1));
        return this;
    }
}
