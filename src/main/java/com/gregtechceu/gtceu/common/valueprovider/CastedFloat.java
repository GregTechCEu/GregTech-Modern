package com.gregtechceu.gtceu.common.valueprovider;

import com.gregtechceu.gtceu.common.data.valueprovider.GTValueProviderTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviderType;
import net.minecraft.util.valueproviders.IntProvider;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

public class CastedFloat extends FloatProvider {

    public static final MapCodec<CastedFloat> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            IntProvider.CODEC.fieldOf("source").forGetter(provider -> provider.source))
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
    public float getMinValue() {
        return this.source.getMinValue();
    }

    @Override
    public float getMaxValue() {
        return this.source.getMaxValue();
    }

    @Override
    public @NotNull FloatProviderType<?> getType() {
        return GTValueProviderTypes.CASTED.get();
    }
}
