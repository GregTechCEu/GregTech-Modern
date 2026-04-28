package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.common.data.GTValueProviderTypes;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class AddedFloat implements FloatProvider {

    public static final MapCodec<AddedFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            FloatProviders.CODEC.fieldOf("source").forGetter(provider -> provider.source),
            FloatProviders.CODEC.fieldOf("modifier").forGetter(provider -> provider.modifier))
            .apply(instance, AddedFloat::new));

    private final FloatProvider source;
    private final FloatProvider modifier;

    public static AddedFloat of(FloatProvider source, FloatProvider multiplier) {
        return new AddedFloat(source, multiplier);
    }

    public AddedFloat(FloatProvider source, FloatProvider modifier) {
        this.source = source;
        this.modifier = modifier;
    }

    @Override
    public float sample(@NotNull RandomSource random) {
        return this.source.sample(random) + this.modifier.sample(random);
    }

    @Override
    public float min() {
        return this.source.min() + this.modifier.min();
    }

    @Override
    public float max() {
        return this.source.max() +
                (this.modifier instanceof ConstantFloat c ? c.value() : this.modifier.max());
    }

    @Override
    public @NotNull MapCodec<? extends FloatProvider> codec() {
        return GTValueProviderTypes.ADDED.get();
    }
}
