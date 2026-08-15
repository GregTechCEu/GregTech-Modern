package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.utils.codec.CodecUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.With;

import java.util.Optional;

public record LargeFluidContent(@With FluidStack stored, @With long amount) {

    // spotless:off
    public static final Codec<FluidStack> OPTIONAL_SINGLE_FLUID_CODEC = ExtraCodecs.optionalEmptyMap(FluidStack.fixedAmountCodec(1))
            .xmap(stack -> stack.orElse(FluidStack.EMPTY),
                    stack -> stack.isEmpty() ? Optional.empty() : Optional.of(stack));
    public static final Codec<LargeFluidContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OPTIONAL_SINGLE_FLUID_CODEC.fieldOf("stored").forGetter(LargeFluidContent::stored),
            CodecUtils.NON_NEGATIVE_LONG.fieldOf("amount").forGetter(LargeFluidContent::amount)
    ).apply(instance, LargeFluidContent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LargeFluidContent> STREAM_CODEC = StreamCodec.composite(
            FluidStack.OPTIONAL_STREAM_CODEC, LargeFluidContent::stored,
            ByteBufCodecs.VAR_LONG, LargeFluidContent::amount,
            LargeFluidContent::new);
    // spotless:on

    public static final LargeFluidContent EMPTY = new LargeFluidContent(FluidStack.EMPTY, 0);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LargeFluidContent(FluidStack otherStored, long otherAmount))) return false;

        return amount() == otherAmount &&
                FluidStack.isSameFluidSameComponents(stored, otherStored);
    }

    @Override
    public int hashCode() {
        int result = FluidStack.hashFluidAndComponents(stored());
        result = 31 * result + Long.hashCode(amount());
        return result;
    }
}
