package com.gregtechceu.gtceu.api.mui.utils;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import org.jetbrains.annotations.NotNull;

public class ColorShade implements IntIterable {

    public static Builder builder(int main) {
        return new Builder(main);
    }

    public final int main;
    private final int[] brighter;
    private final int[] darker;
    private final int[] all;

    private ColorShade(int main, int[] brighter, int[] darker) {
        this.main = main;
        this.brighter = brighter;
        this.darker = darker;
        this.all = new int[brighter.length + darker.length + 1];
        int k = 0;
        for (int i = brighter.length - 1; i >= 0; i--) {
            this.all[k++] = brighter[i];
        }
        this.all[k++] = this.main;
        for (int j : darker) {
            this.all[k++] = j;
        }
    }

    public int darker(int index) {
        return this.darker[index];
    }

    public int brighter(int index) {
        return this.brighter[index];
    }

    @NotNull
    @Override
    public IntIterator iterator() {
        return IntIterators.wrap(this.all);
    }

    public static class Builder {

        private final int main;
        private final IntArrayList darker = new IntArrayList();
        private final IntArrayList brighter = new IntArrayList();

        public Builder(int main) {
            this.main = main;
        }

        public Builder addDarker(int... darker) {
            this.darker.addElements(this.darker.size(), darker, 0, darker.length);
            return this;
        }

        public Builder addBrighter(int... brighter) {
            this.brighter.addElements(this.brighter.size(), brighter, 0, brighter.length);
            return this;
        }

        public ColorShade build() {
            this.darker.trim();
            this.brighter.trim();
            return new ColorShade(this.main, this.brighter.elements(), this.darker.elements());
        }
    }
}
