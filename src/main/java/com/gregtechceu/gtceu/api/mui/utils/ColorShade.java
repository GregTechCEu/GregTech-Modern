package com.gregtechceu.gtceu.api.mui.utils;

import net.minecraft.util.Mth;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ColorShade implements IntIterable {

    public static Builder builder(String name, int main) {
        return new Builder(name, main);
    }

    private static final Map<String, ColorShade> COLOR_SHADES = new Object2ObjectOpenHashMap<>();

    @Nullable
    public static ColorShade getFromName(String name) {
        return COLOR_SHADES.get(name);
    }

    @UnmodifiableView
    public static Collection<ColorShade> getAll() {
        return Collections.unmodifiableCollection(COLOR_SHADES.values());
    }

    public final String name;
    public final int main;
    private final int[] brighter;
    private final int[] darker;
    private final int[] all;

    private ColorShade(String name, int main, int[] brighter, int[] darker) {
        this.name = name;
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
        COLOR_SHADES.put(name, this);
    }

    public int darker(int index) {
        return this.darker[index];
    }

    public int darkerSafe(int index) {
        return this.darker[Mth.clamp(index, 0, this.darker.length - 1)];
    }

    public int brighter(int index) {
        return this.brighter[index];
    }

    public int brighterSafe(int index) {
        return this.brighter[Mth.clamp(index, 0, this.brighter.length - 1)];
    }

    @NotNull
    @Override
    public IntIterator iterator() {
        return IntIterators.wrap(this.all);
    }

    public static class Builder {

        private final String name;
        private final int main;
        private final IntArrayList darker = new IntArrayList();
        private final IntArrayList brighter = new IntArrayList();

        public Builder(String name, int main) {
            this.name = name;
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
            return new ColorShade(this.name, this.main, this.brighter.elements(), this.darker.elements());
        }
    }
}
