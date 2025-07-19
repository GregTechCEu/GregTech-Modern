package com.gregtechceu.gtceu.syncdata.data_transformers.collections;

import com.gregtechceu.gtceu.syncdata.IValueTransformer;

import net.minecraft.nbt.*;

public class PrimitiveArrayTransformers {

    public static class IntArrayTransformer implements IValueTransformer<int[]> {

        @Override
        public Tag serializeNBT(int[] value, boolean isSync, boolean isFullSync) {
            return new IntArrayTag(value);
        }

        @Override
        public int[] deserializeNBT(Tag tag, int[] currentVal, boolean isSync) {
            if (tag instanceof IntArrayTag arr) return arr.getAsIntArray();
            return new int[0];
        }
    }

    public static class LongArrayTransformer implements IValueTransformer<long[]> {

        @Override
        public Tag serializeNBT(long[] value, boolean isSync, boolean isFullSync) {
            return new LongArrayTag(value);
        }

        @Override
        public long[] deserializeNBT(Tag tag, long[] currentVal, boolean isSync) {
            if (tag instanceof LongArrayTag arr) return arr.getAsLongArray();
            return new long[0];
        }
    }

    public static class ByteArrayTransformer implements IValueTransformer<byte[]> {

        @Override
        public Tag serializeNBT(byte[] value, boolean isSync, boolean isFullSync) {
            return new ByteArrayTag(value);
        }

        @Override
        public byte[] deserializeNBT(Tag tag, byte[] currentVal, boolean isSync) {
            if (tag instanceof ByteArrayTag arr) return arr.getAsByteArray();
            return new byte[0];
        }
    }
}
