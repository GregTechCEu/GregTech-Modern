package com.gregtechceu.gtceu.utils;

import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function7;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.datafixers.util.Function8;
import io.netty.buffer.ByteBuf;
import org.checkerframework.checker.units.qual.C;

import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;

import javax.annotation.ParametersAreNonnullByDefault;

public class StreamCodecUtils {

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    public static <B extends ByteBuf, K, V, M extends Map<K, V>> StreamCodec<B, M> dispatchMap(
                                                                                               final IntFunction<? extends M> mapCreationFunction,
                                                                                               final StreamCodec<? super B, K> keyCodec,
                                                                                               final Function<K, StreamCodec<? super B, V>> valueFunction) {
        return new StreamCodec<>() {

            public void encode(B buf, M val) {
                ByteBufCodecs.writeCount(buf, val.size(), Integer.MAX_VALUE);
                val.forEach((key, value) -> {
                    keyCodec.encode(buf, key);
                    valueFunction.apply(key).encode(buf, value);
                });
            }

            public M decode(B buf) {
                int i = ByteBufCodecs.readCount(buf, Integer.MAX_VALUE);
                M m = mapCreationFunction.apply(Math.min(i, 65536));

                for (int j = 0; j < i; j++) {
                    K k = keyCodec.decode(buf);
                    V v = valueFunction.apply(k).decode(buf);
                    m.put(k, v);
                }

                return m;
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8> StreamCodec<B, C> composite(
                                                                                     final StreamCodec<? super B, T1> codec1,
                                                                                     final Function<C, T1> getter1,
                                                                                     final StreamCodec<? super B, T2> codec2,
                                                                                     final Function<C, T2> getter2,
                                                                                     final StreamCodec<? super B, T3> codec3,
                                                                                     final Function<C, T3> getter3,
                                                                                     final StreamCodec<? super B, T4> codec4,
                                                                                     final Function<C, T4> getter4,
                                                                                     final StreamCodec<? super B, T5> codec5,
                                                                                     final Function<C, T5> getter5,
                                                                                     final StreamCodec<? super B, T6> codec6,
                                                                                     final Function<C, T6> getter6,
                                                                                     final StreamCodec<? super B, T7> codec7,
                                                                                     final Function<C, T7> getter7,
                                                                                     final StreamCodec<? super B, T8> codec8,
                                                                                     final Function<C, T8> getter8,
                                                                                     final Function8<T1, T2, T3, T4, T5, T6, T7, T8, C> p_331335_) {
        return new StreamCodec<>() {

            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                return p_331335_.apply(t1, t2, t3, t4, t5, t6, t7, t8);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
            }
        };
    }

    public static <B, C, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> StreamCodec<B, C> composite(
            final StreamCodec<? super B, T1> codec1,
            final Function<C, T1> getter1,
            final StreamCodec<? super B, T2> codec2,
            final Function<C, T2> getter2,
            final StreamCodec<? super B, T3> codec3,
            final Function<C, T3> getter3,
            final StreamCodec<? super B, T4> codec4,
            final Function<C, T4> getter4,
            final StreamCodec<? super B, T5> codec5,
            final Function<C, T5> getter5,
            final StreamCodec<? super B, T6> codec6,
            final Function<C, T6> getter6,
            final StreamCodec<? super B, T7> codec7,
            final Function<C, T7> getter7,
            final StreamCodec<? super B, T8> codec8,
            final Function<C, T8> getter8,
            final StreamCodec<? super B, T9> codec9,
            final Function<C, T9> getter9,
            final StreamCodec<? super B, T10> codec10,
            final Function<C, T10> getter10,
            final StreamCodec<? super B, T11> codec11,
            final Function<C, T11> getter11,
            final StreamCodec<? super B, T12> codec12,
            final Function<C, T12> getter12,
            final Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, C> constructor) {
        return new StreamCodec<>() {

            @Override
            public C decode(B buffer) {
                T1 t1 = codec1.decode(buffer);
                T2 t2 = codec2.decode(buffer);
                T3 t3 = codec3.decode(buffer);
                T4 t4 = codec4.decode(buffer);
                T5 t5 = codec5.decode(buffer);
                T6 t6 = codec6.decode(buffer);
                T7 t7 = codec7.decode(buffer);
                T8 t8 = codec8.decode(buffer);
                T9 t9 = codec9.decode(buffer);
                T10 t10 = codec10.decode(buffer);
                T11 t11 = codec11.decode(buffer);
                T12 t12 = codec12.decode(buffer);
                return constructor.apply(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
            }

            @Override
            public void encode(B buffer, C object) {
                codec1.encode(buffer, getter1.apply(object));
                codec2.encode(buffer, getter2.apply(object));
                codec3.encode(buffer, getter3.apply(object));
                codec4.encode(buffer, getter4.apply(object));
                codec5.encode(buffer, getter5.apply(object));
                codec6.encode(buffer, getter6.apply(object));
                codec7.encode(buffer, getter7.apply(object));
                codec8.encode(buffer, getter8.apply(object));
                codec9.encode(buffer, getter9.apply(object));
                codec10.encode(buffer, getter10.apply(object));
                codec11.encode(buffer, getter11.apply(object));
                codec12.encode(buffer, getter12.apply(object));
            }
        };
    }
}
