package com.gregtechceu.gtceu.api.item.components;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ToolCharge(long maxCharge, long charge) {
    public static final Codec<ToolCharge> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.LONG.fieldOf("max_charge").forGetter(ToolCharge::maxCharge),
        Codec.LONG.fieldOf("charge").forGetter(ToolCharge::charge)
    ).apply(instance, ToolCharge::new));
    public static final StreamCodec<ByteBuf, ToolCharge> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.VAR_LONG, ToolCharge::maxCharge,
        ByteBufCodecs.VAR_LONG, ToolCharge::charge,
        ToolCharge::new
    );
}
