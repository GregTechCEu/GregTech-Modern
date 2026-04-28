package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.common.data.GTValueProviderTypes;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class MultipliedFloat implements FloatProvider {

    public static final MapCodec<MultipliedFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FloatProviders.CODEC.fieldOf("source").forGetter(provider -> provider.source),
            FloatProviders.CODEC.fieldOf("multiplier").forGetter(provider -> provider.multiplier))
            .apply(instance, MultipliedFloat::new));

    private final FloatProvider source;
    private final FloatProvider multiplier;

    public static MultipliedFloat of(FloatProvider source, FloatProvider multiplier) {
        return new MultipliedFloat(source, multiplier);
    }

    public MultipliedFloat(FloatProvider source, FloatProvider multiplier) {
        this.source = source;
        this.multiplier = multiplier;
    }

    @Override
    public float sample(@NotNull RandomSource random) {
        return this.source.sample(random) * this.multiplier.sample(random);
    }

    @Override
    public float min() {
        return this.source.min() * this.multiplier.min();
    }

    @Override
    public float max() {
        return this.source.max() *
                (this.multiplier instanceof ConstantFloat c ? c.value() : this.multiplier.max());
    }

    @Override
    public @NotNull MapCodec<? extends FloatProvider> codec() {
        return GTValueProviderTypes.MULTIPLIED.get();
    }
}
