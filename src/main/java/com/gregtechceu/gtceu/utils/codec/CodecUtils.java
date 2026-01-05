package com.gregtechceu.gtceu.utils.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class CodecUtils {

    public static final Codec<Long> NON_NEGATIVE_LONG = longRangeWithMessage(0, Long.MAX_VALUE,
            value -> "Value must be non-negative: " + value);
    public static final Codec<Long> POSITIVE_LONG = longRangeWithMessage(1, Long.MAX_VALUE,
            value -> "Value must be positive: " + value);

    public static Codec<Long> longRangeWithMessage(long min, long max, Function<Long, String> errorMessage) {
        return Codec.LONG
                .validate(
                        value -> value.compareTo(min) >= 0 && value.compareTo(max) <= 0 ? DataResult.success(value) :
                                DataResult.error(() -> errorMessage.apply(value)));
    }

    public static <T, A> DataResult<A> encodeMap(T value, MapCodec<T> codec, DynamicOps<A> ops) {
        return codec.encode(value, ops, ops.mapBuilder()).build(ops.empty());
    }

    public static <T> MapCodec<T> nullableOptionalFieldOf(Codec<T> self, final String name, final T defaultValue) {
        return nullableOptionalFieldOf(self, name, defaultValue, false);
    }

    public static <T> MapCodec<T> lenientNullableOptionalFieldOf(Codec<T> self, final String name,
                                                                 final T defaultValue) {
        return nullableOptionalFieldOf(self, name, defaultValue, true);
    }

    public static <T> MapCodec<T> nullableOptionalFieldOf(Codec<T> self, final String name, final T defaultValue,
                                                          final boolean lenient) {
        return Codec.optionalField(name, self, lenient).xmap(
                o -> o.orElse(defaultValue),
                a -> a == null || Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a));
    }
}
