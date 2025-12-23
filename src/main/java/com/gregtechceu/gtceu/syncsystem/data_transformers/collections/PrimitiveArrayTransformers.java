package com.gregtechceu.gtceu.syncsystem.data_transformers.collections;

import com.gregtechceu.gtceu.syncsystem.ISyncManaged;
import com.gregtechceu.gtceu.syncsystem.IValueTransformer;

import net.minecraft.nbt.*;

public class PrimitiveArrayTransformers {

    public static class IntArrayTransformer implements IValueTransformer<int[]> {

        @Override
        public Tag serializeNBT(int[] value, ISyncManaged holder) {
            return new IntArrayTag(value);
        }

        @Override
        public int[] deserializeNBT(Tag tag, ISyncManaged holder, int[] currentVal) {
            if (tag instanceof IntArrayTag arr) return arr.getAsIntArray();
            return new int[0];
        }
    }

    public static class LongArrayTransformer implements IValueTransformer<long[]> {

        @Override
        public Tag serializeNBT(long[] value, ISyncManaged holder) {
            return new LongArrayTag(value);
        }

        @Override
        public long[] deserializeNBT(Tag tag, ISyncManaged holder, long[] currentVal) {
            if (tag instanceof LongArrayTag arr) return arr.getAsLongArray();
            return new long[0];
        }
    }

    public static class ByteArrayTransformer implements IValueTransformer<byte[]> {

        @Override
        public Tag serializeNBT(byte[] value, ISyncManaged holder) {
            return new ByteArrayTag(value);
        }

        @Override
        public byte[] deserializeNBT(Tag tag, ISyncManaged holder, byte[] currentVal) {
            if (tag instanceof ByteArrayTag arr) return arr.getAsByteArray();
            return new byte[0];
        }
    }
}
