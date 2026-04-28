package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.common.data.GTValueProviderTypes;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class CastedFloat implements FloatProvider {

    public static final MapCodec<CastedFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntProviders.CODEC.fieldOf("source").forGetter(provider -> provider.source))
            .apply(instance, CastedFloat::new));

    private final IntProvider source;

    public static CastedFloat of(IntProvider source) {
        return new CastedFloat(source);
    }

    public CastedFloat(IntProvider source) {
        this.source = source;
    }

    @Override
    public float sample(@NotNull RandomSource random) {
        return this.source.sample(random);
    }

    @Override
    public float min() {
        return this.source.minInclusive();
    }

    @Override
    public float max() {
        return this.source.maxInclusive();
    }

    @Override
    public @NotNull MapCodec<? extends FloatProvider> codec() {
        return GTValueProviderTypes.CASTED.get();
    }
}
