package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.common.data.GTValueProviderTypes;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class AddedFloat extends FloatProvider {

    public static final Codec<AddedFloat> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FloatProvider.CODEC.fieldOf("source").forGetter(provider -> provider.source),
            FloatProvider.CODEC.fieldOf("modifier").forGetter(provider -> provider.modifier))
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
    public float getMinValue() {
        return this.source.getMinValue() + this.modifier.getMinValue();
    }

    @Override
    public float getMaxValue() {
        return this.source.getMaxValue() +
                (this.modifier instanceof ConstantFloat c ? c.getValue() : this.modifier.getMaxValue());
    }

    @Override
    public @NotNull FloatProviderType<?> getType() {
        return GTValueProviderTypes.ADDED.get();
    }
}
