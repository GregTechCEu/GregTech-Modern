package com.gregtechceu.gtceu.common.valueprovider;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformFloat;

/** Checks run in NeoForge's test loader while the full GT source port is incomplete. */
public final class PortValueProviderCheck {
    private static <T> T roundTrip(com.mojang.serialization.MapCodec<T> codec, String json) {
        var input = com.google.gson.JsonParser.parseString(json);
        var decoded = codec.codec().parse(com.mojang.serialization.JsonOps.INSTANCE, input).getOrThrow();
        var encoded = codec.codec().encodeStart(com.mojang.serialization.JsonOps.INSTANCE, decoded).getOrThrow();
        org.junit.jupiter.api.Assertions.assertEquals(input, encoded);
        return decoded;
    }

    @org.junit.jupiter.api.Test
    void serializedFieldsRemainCompatible() {
        var random = RandomSource.create(42);
        equal(5, roundTrip(AddedFloat.CODEC, "{\"source\":3,\"modifier\":2}").sample(random));
        equal(6, roundTrip(MultipliedFloat.CODEC, "{\"source\":3,\"multiplier\":2}").sample(random));
        equal(7, roundTrip(CastedFloat.CODEC, "{\"source\":7}").sample(random));
        equal(3, roundTrip(FlooredInt.CODEC, "{\"source\":3.75}").sample(random));
    }

    private static void equal(float expected, float actual) {
        if (expected != actual) throw new AssertionError(expected + " != " + actual);
    }

    @org.junit.jupiter.api.Test
    void samplingBoundsAndCodecIdentity() {
        var random = RandomSource.create(42);
        var sum = AddedFloat.of(ConstantFloat.of(3), ConstantFloat.of(2));
        equal(5, sum.sample(random));
        equal(5, sum.min());
        equal(5, sum.max());
        var product = MultipliedFloat.of(UniformFloat.of(2, 4), ConstantFloat.of(3));
        equal(6, product.min());
        equal(12, product.max());
        for (int i = 0; i < 100; i++) {
            float sample = product.sample(random);
            if (sample < product.min() || sample > product.max()) throw new AssertionError(sample);
        }
        var cast = CastedFloat.of(ConstantInt.of(7));
        equal(7, cast.sample(random));
        equal(7, cast.min());
        equal(7, cast.max());
        var integer = FlooredInt.of(ConstantFloat.of(3.75f));
        equal(3, integer.sample(random));
        equal(3, integer.minInclusive());
        equal(3, integer.maxInclusive());
        // Preserve the existing cast-to-int behavior, including negative inputs.
        equal(-3, FlooredInt.of(ConstantFloat.of(-3.75f)).sample(random));
        if (sum.codec() != AddedFloat.CODEC || product.codec() != MultipliedFloat.CODEC ||
                cast.codec() != CastedFloat.CODEC || integer.codec() != FlooredInt.CODEC) {
            throw new AssertionError("Provider codec identity changed");
        }
        System.out.println("4 GT provider checks passed (sampling, bounds, codec identity)");
    }
}
