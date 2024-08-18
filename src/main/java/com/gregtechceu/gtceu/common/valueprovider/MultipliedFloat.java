package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.data.valueprovider.GTValueProviderTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class MultipliedFloat extends FloatProvider {

    public static final MapCodec<MultipliedFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FloatProvider.CODEC.fieldOf("source").forGetter(provider -> provider.source),
            FloatProvider.CODEC.fieldOf("multiplier").forGetter(provider -> provider.multiplier))
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
    public float getMinValue() {
        return this.source.getMinValue() * this.multiplier.getMinValue();
    }

    @Override
    public float getMaxValue() {
        return this.source.getMaxValue() * this.multiplier.getMaxValue();
    }

    @Override
    public @NotNull FloatProviderType<?> getType() {
        return GTValueProviderTypes.MULTIPLIED.get();
    }
}
