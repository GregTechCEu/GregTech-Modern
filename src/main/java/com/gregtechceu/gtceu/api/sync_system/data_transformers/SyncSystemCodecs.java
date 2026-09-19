package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import io.netty.buffer.ByteBuf;

// Extra codecs/stream codecs used in the sync system
public class SyncSystemCodecs {

    // MC why doesn't this exist by default
    public static final PrimitiveCodec<Character> CHAR = new PrimitiveCodec<>() {

        @Override
        public <T> DataResult<Character> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input)
                    .map(n -> (char) n.intValue());
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Character value) {
            return ops.createShort((short) value.charValue());
        }

        @Override
        public String toString() {
            return "Char";
        }
    };

    public static final StreamCodec<ByteBuf, Character> CHAR_STREAM = new StreamCodec<>() {

        public Character decode(ByteBuf buffer) {
            return buffer.readChar();
        }

        public void encode(ByteBuf buffer, Character value) {
            buffer.writeChar(value);
        }
    };

    public static final StreamCodec<FriendlyByteBuf, int[]> INT_ARRAY_STREAM = new StreamCodec<>() {

        public int[] decode(FriendlyByteBuf buffer) {
            return buffer.readVarIntArray();
        }

        public void encode(FriendlyByteBuf buffer, int[] value) {
            buffer.writeVarIntArray(value);
        }
    };

    public static final StreamCodec<FriendlyByteBuf, long[]> LONG_ARRAY_STREAM = new StreamCodec<>() {

        public long[] decode(FriendlyByteBuf buffer) {
            return buffer.readLongArray();
        }

        public void encode(FriendlyByteBuf buffer, long[] value) {
            buffer.writeLongArray(value);
        }
    };
}
