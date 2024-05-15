package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

public record SimpleEnergyContent(long maxCharge, long charge, boolean infinite, boolean dischargeMode) {

    public static final Codec<SimpleEnergyContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.LONG.fieldOf("max_charge").forGetter(SimpleEnergyContent::maxCharge),
            Codec.LONG.fieldOf("charge").forGetter(SimpleEnergyContent::charge),
            Codec.BOOL.fieldOf("infinite").forGetter(SimpleEnergyContent::infinite),
            Codec.BOOL.fieldOf("discharge_mode").forGetter(SimpleEnergyContent::dischargeMode))
            .apply(instance, SimpleEnergyContent::new));
    public static final StreamCodec<ByteBuf, SimpleEnergyContent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, SimpleEnergyContent::maxCharge,
            ByteBufCodecs.VAR_LONG, SimpleEnergyContent::charge,
            ByteBufCodecs.BOOL, SimpleEnergyContent::infinite,
            ByteBufCodecs.BOOL, SimpleEnergyContent::dischargeMode,
            SimpleEnergyContent::new);

    public SimpleEnergyContent(long maxCharge, long charge) {
        this(maxCharge, charge, false, false);
    }

    public SimpleEnergyContent withMaxCharge(long maxCharge) {
        return new SimpleEnergyContent(maxCharge, charge, infinite, dischargeMode);
    }

    public SimpleEnergyContent withCharge(long charge) {
        return new SimpleEnergyContent(maxCharge, charge, infinite, dischargeMode);
    }

    public SimpleEnergyContent withInfinite(boolean infinite) {
        return new SimpleEnergyContent(maxCharge, charge, infinite, dischargeMode);
    }

    public SimpleEnergyContent withDischargeMode(boolean dischargeMode) {
        return new SimpleEnergyContent(maxCharge, charge, infinite, dischargeMode);
    }
}
