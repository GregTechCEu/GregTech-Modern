package com.gregtechceu.gtceu.utils.codec;

import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.util.ExtraCodecs;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import it.unimi.dsi.fastutil.ints.IntIntPair;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public final class GTCodecUtils {

    private GTCodecUtils() {}

    public static final Codec<Long> NON_NEGATIVE_LONG = longRangeWithMessage(0, Long.MAX_VALUE,
            (val) -> "Value must be non-negative: " + val);
    public static final Codec<Long> POSITIVE_LONG = longRangeWithMessage(1, Long.MAX_VALUE,
            (val) -> "Value must be positive: " + val);

    public static final Codec<IntIntPair> FAST_UTIL_INT_PAIR_CODEC = Codec.pair(Codec.INT, Codec.INT)
            .xmap(v -> IntIntPair.of(v.getFirst(), v.getSecond()),
                    v -> com.mojang.datafixers.util.Pair.of(v.firstInt(), v.secondInt()));

    public static Codec<Long> longRangeWithMessage(long min, long max, Function<Long, String> errorMessage) {
        return ExtraCodecs.validate(Codec.LONG, (val) -> {
            if (val.compareTo(min) >= 0 && val.compareTo(max) <= 0) {
                return DataResult.success(val);
            } else {
                return DataResult.error(() -> errorMessage.apply(val));
            }
        });
    }

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

    // Uses a list of pairs internally because the default map codec can't handle non-string primitive keys.
    public static <K, V> Codec<Map<K, V>> primitiveKeyedMap(Codec<K> keyCodec,
                                                            Codec<V> valueCodec) {
        return Codec.pair(keyCodec, valueCodec).listOf().xmap(list -> {
            Map<K, V> map = new HashMap<>(list.size());
            for (var pair : list) {
                map.put(pair.getFirst(), pair.getSecond());
            }
            return map;
        }, v -> v.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue())).toList());
    }

    public static <R, C, V> Codec<Table<R, C, V>> table(Codec<R> rowCodec, Codec<C> colCodec, Codec<V> valueCodec) {
        var colMap = GTCodecUtils.primitiveKeyedMap(colCodec, valueCodec);
        var rowMap = GTCodecUtils.primitiveKeyedMap(rowCodec, colMap);

        return rowMap.xmap(v -> {
            Table<R, C, V> table = HashBasedTable.create();
            for (var rowEntry : v.entrySet()) {
                var row = rowEntry.getKey();
                for (var entry : rowEntry.getValue().entrySet()) {
                    table.put(row, entry.getKey(), entry.getValue());
                }
            }
            return table;
        }, Table::rowMap);
    }

    public static Codec<Long> longRange(long min, long max) {
        return longRangeWithMessage(min, max, (val) -> "Value must be within range [" + min + ";" + max + "]: " + val);
    }

    public static <T> T unboxEither(Either<T, T> either) {
        return either.map(Function.identity(), Function.identity());
    }

    public static <T> Codec<Supplier<T>> lazyParsingCodec(Codec<T> delegate) {
        return new LazyParsingCodec<>(delegate);
    }

    private record LazyParsingCodec<A>(Codec<A> codec) implements Codec<Supplier<A>> {

        @Override
        public <T> DataResult<Pair<Supplier<A>, T>> decode(DynamicOps<T> ops, T input) {
            return DataResult.success(Pair.of(GTMemoizer.memoize(() -> deferredDecode(ops, input)), input));
        }

        @Override
        public <T> DataResult<T> encode(Supplier<A> input, DynamicOps<T> ops, T prefix) {
            return input.get() == null ? DataResult.success(prefix) : this.codec.encode(input.get(), ops, prefix);
        }

        private <T> A deferredDecode(DynamicOps<T> ops, T input) {
            return this.codec.decode(ops, input).get()
                    .map(Pair::getFirst, partial -> {
                        throw new IllegalStateException("Unable to parse deferred value: " + partial.message());
                    });
        }
    }
}
