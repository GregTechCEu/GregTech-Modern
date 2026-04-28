package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.common.data.GTValueProviderTypes;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.util.valueproviders.IntProvider;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class FlooredInt implements IntProvider {

    public static final MapCodec<FlooredInt> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FloatProviders.CODEC.fieldOf("source").forGetter(provider -> provider.source))
            .apply(instance, FlooredInt::new));

    private final FloatProvider source;

    public static FlooredInt of(FloatProvider source) {
        return new FlooredInt(source);
    }

    public FlooredInt(FloatProvider source) {
        this.source = source;
    }

    @Override
    public int sample(@NotNull RandomSource random) {
        return (int) this.source.sample(random);
    }

    @Override
    public int minInclusive() {
        return (int) this.source.min();
    }

    @Override
    public int maxInclusive() {
        return (int) this.source.max();
    }

    @Override
    public @NotNull MapCodec<? extends IntProvider> codec() {
        return GTValueProviderTypes.FLOORED.get();
    }
}
