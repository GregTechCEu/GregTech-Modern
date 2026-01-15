package com.gregtechceu.gtceu.common.item.datacomponents;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;

public record DataItem(boolean requireDataBank, int capacity) {

    // spotless:off
    public static final Codec<DataItem> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.fieldOf("requires_data_bank").forGetter(DataItem::requireDataBank),
            ExtraCodecs.POSITIVE_INT.fieldOf("capacity").forGetter(DataItem::capacity)
    ).apply(instance, DataItem::new));
    public static final StreamCodec<ByteBuf, DataItem> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, DataItem::requireDataBank,
            ByteBufCodecs.VAR_INT, DataItem::capacity,
            DataItem::new
    );
    // spotless:on

    public DataItem {
        Preconditions.checkArgument(capacity > 0, "Capacity must be positive");
    }
}
