package com.gregtechceu.gtceu.api.multiblock.pattern;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

public class PatternAisle {

    @Getter
    protected int minRepeats, maxRepeats;
    @Setter
    @Getter
    protected int actualRepeats;
    @Getter
    protected final String[] pattern;

    public PatternAisle(int repeats, char[][] pattern) {
        this.pattern = new String[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            this.pattern[i] = new String(pattern[i]);
        }
        this.minRepeats = this.maxRepeats = repeats;
    }

    public PatternAisle(int minRepeats, int maxRepeats, String[] pattern) {
        this.pattern = pattern;
        this.minRepeats = minRepeats;
        this.maxRepeats = maxRepeats;
    }

    public PatternAisle(int repeats, String[] pattern) {
        this(repeats, repeats, pattern);
    }

    public PatternAisle(String[] pattern) {
        this(1, pattern);
    }

    public PatternAisle(char[][] pattern) {
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
            int pos = pattern[strI].indexOf(c);
            if (pos != -1) return new int[] { strI, pos };
        }
        return null;
    }

    public char charAt(int stringI, int charI) {
        return pattern[stringI].charAt(charI);
    }

    public int getStringCount() {
        return pattern.length;
    }

    public int getCharCount() {
        return pattern[0].length();
    }

    public PatternAisle copy() {
        PatternAisle c = new PatternAisle(minRepeats, maxRepeats, pattern.clone());
        c.actualRepeats = this.actualRepeats;
        return c;
    }
}
