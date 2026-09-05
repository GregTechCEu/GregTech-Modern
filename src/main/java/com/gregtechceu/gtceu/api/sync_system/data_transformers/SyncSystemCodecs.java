package com.gregtechceu.gtceu.api.sync_system.data_transformers;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;

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
}
