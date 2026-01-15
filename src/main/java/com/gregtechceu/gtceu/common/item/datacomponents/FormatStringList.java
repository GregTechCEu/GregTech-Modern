package com.gregtechceu.gtceu.common.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record FormatStringList(List<String> lines) {

    public static final Codec<FormatStringList> CODEC = Codec.STRING.listOf()
            .xmap(FormatStringList::new, FormatStringList::lines);
    public static final StreamCodec<ByteBuf, FormatStringList> STREAM_CODEC = ByteBufCodecs.STRING_UTF8
            .apply(ByteBufCodecs.list())
            .map(FormatStringList::new, FormatStringList::lines);

    public static final FormatStringList EMPTY = new FormatStringList(Collections.emptyList());

    public FormatStringList {
        lines = Collections.unmodifiableList(lines);
    }

    public Mutable mutable() {
        return new Mutable(this.lines);
    }

    public static class Mutable extends AbstractList<String> {

        private final List<String> lines;

        public Mutable(List<String> lines) {
            this.lines = new ArrayList<>(lines);
        }

        public FormatStringList toImmutable() {
            return new FormatStringList(this.lines);
        }

        @Override
        public String get(int index) {
            return lines.get(index);
        }

        @Override
        public int size() {
            return lines.size();
        }

        @Override
        public boolean add(String s) {
            return lines.add(s);
        }

        @Override
        public boolean remove(Object o) {
            return lines.remove(o);
        }

        @Override
        public String remove(int index) {
            return lines.remove(index);
        }
    }
}
