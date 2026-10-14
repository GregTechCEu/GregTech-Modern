package com.gregtechceu.gtceu.api.multiblock.pattern;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class PatternSlice {

    @Getter
    protected int minRepeats, maxRepeats;
    @Setter
    @Getter
    protected int actualRepeats;
    @Getter
    protected final char[][] pattern;

    private static char[][] toCharArray(String[] pattern) {
        return Arrays.stream(pattern)
                .map(String::toCharArray)
                .toArray(char[][]::new);
    }

    public PatternSlice(int minRepeats, int maxRepeats, char[][] pattern) {
        this.pattern = pattern;
        this.minRepeats = minRepeats;
        this.maxRepeats = maxRepeats;
    }

    public PatternSlice(int minRepeats, int maxRepeats, String[] pattern) {
        this(minRepeats, maxRepeats, toCharArray(pattern));
    }

    public PatternSlice(int repeats, char[][] pattern) {
        this(repeats, repeats, pattern);
    }

    public PatternSlice(int repeats, String[] pattern) {
        this(repeats, repeats, pattern);
    }

    public PatternSlice(char[][] pattern) {
        this(1, pattern);
    }

    public PatternSlice(String[] pattern) {
        this(1, pattern);
    }

    public void setRepeats(int minRepeats, int maxRepeats) {
        this.minRepeats = minRepeats;
        this.maxRepeats = maxRepeats;
    }

    public void setRepeats(int repeats) {
        this.minRepeats = repeats;
        this.maxRepeats = repeats;
    }

    public int @Nullable [] firstInstanceOf(char c) {
        for (int strI = 0; strI < pattern.length; strI++) {
            int pos = ArrayUtils.indexOf(pattern[strI], c);
            if (pos != -1) return new int[] { strI, pos };
        }
        return null;
    }

    public char charAt(int stringI, int charI) {
        return pattern[stringI][charI];
    }

    public PatternSlice copy() {
        PatternSlice c = new PatternSlice(minRepeats, maxRepeats, pattern.clone());
        c.actualRepeats = this.actualRepeats;
        return c;
    }
}
