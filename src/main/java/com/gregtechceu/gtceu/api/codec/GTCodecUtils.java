package com.gregtechceu.gtceu.api.codec;

import net.minecraft.util.ExtraCodecs;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import java.util.function.Function;

public class GTCodecUtils {

    public static final Codec<Long> NON_NEGATIVE_LONG = longRangeWithMessage(0, Long.MAX_VALUE,
            (val) -> "Value must be non-negative: " + val);
    public static final Codec<Long> POSITIVE_LONG = longRangeWithMessage(1, Long.MAX_VALUE,
            (val) -> "Value must be positive: " + val);

    public static Codec<Long> longRangeWithMessage(long min, long max, Function<Long, String> errorMessage) {
        return ExtraCodecs.validate(Codec.LONG, (val) -> {
            if (val.compareTo(min) >= 0 && val.compareTo(max) <= 0) {
                return DataResult.success(val);
            } else {
                return DataResult.error(() -> errorMessage.apply(val));
            }
        });
    }

    public static Codec<Long> longRange(long min, long max) {
        return longRangeWithMessage(min, max, (val) -> "Value must be within range [" + min + ";" + max + "]: " + val);
    }

    public static <T> T unboxEither(Either<T, T> either) {
        return either.map(Function.identity(), Function.identity());
    }
}
