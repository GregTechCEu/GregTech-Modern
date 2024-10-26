package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.common.data.GTValueProviderTypes;

import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviderType;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class FlooredInt extends IntProvider {

    public static final Codec<FlooredInt> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            FloatProvider.CODEC.fieldOf("source").forGetter(provider -> provider.source))
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
    public int getMinValue() {
        return (int) this.source.getMinValue();
    }

    @Override
    public int getMaxValue() {
        return (int) this.source.getMaxValue();
    }

    @Override
    public @NotNull IntProviderType<?> getType() {
        return GTValueProviderTypes.FLOORED.get();
    }
}
