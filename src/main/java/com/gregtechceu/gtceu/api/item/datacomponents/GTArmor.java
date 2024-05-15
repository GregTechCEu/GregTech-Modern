package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

public record GTArmor(
                      byte toggleTimer,
                      boolean hover,
                      short burnTimer,
                      boolean canShare,
                      boolean nightVision,
                      byte consumerTicks) {

    public static final Codec<GTArmor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("toggle_timer").forGetter(GTArmor::toggleTimer),
            Codec.BOOL.fieldOf("hover").forGetter(GTArmor::hover),
            Codec.SHORT.fieldOf("burn_timer").forGetter(GTArmor::burnTimer),
            Codec.BOOL.fieldOf("can_share").forGetter(GTArmor::canShare),
            Codec.BOOL.fieldOf("nightvision").forGetter(GTArmor::nightVision),
            Codec.BYTE.fieldOf("consumer_ticks").forGetter(GTArmor::consumerTicks)).apply(instance, GTArmor::new));
    public static final StreamCodec<ByteBuf, GTArmor> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, GTArmor::toggleTimer,
            ByteBufCodecs.BOOL, GTArmor::hover,
            ByteBufCodecs.SHORT, GTArmor::burnTimer,
            ByteBufCodecs.BOOL, GTArmor::canShare,
            ByteBufCodecs.BOOL, GTArmor::nightVision,
            ByteBufCodecs.BYTE, GTArmor::consumerTicks,
            GTArmor::new);

    public GTArmor() {
        this((byte) 0, false, (short) 0, false, false, (byte) 0);
    }

    public GTArmor setToggleTimer(byte toggleTimer) {
        return new GTArmor(toggleTimer, hover, burnTimer, canShare, nightVision, consumerTicks);
    }

    public GTArmor setHover(boolean hover) {
        return new GTArmor(toggleTimer, hover, burnTimer, canShare, nightVision, consumerTicks);
    }

    public GTArmor setBurnTimer(short burnTimer) {
        return new GTArmor(toggleTimer, hover, burnTimer, canShare, nightVision, consumerTicks);
    }

    public GTArmor setCanShare(boolean canShare) {
        return new GTArmor(toggleTimer, hover, burnTimer, canShare, nightVision, consumerTicks);
    }

    public GTArmor setNightVision(boolean nightVision) {
        return new GTArmor(toggleTimer, hover, burnTimer, canShare, nightVision, consumerTicks);
    }

    public GTArmor setConsumerTicks(byte consumerTicks) {
        return new GTArmor(toggleTimer, hover, burnTimer, canShare, nightVision, consumerTicks);
    }
}
