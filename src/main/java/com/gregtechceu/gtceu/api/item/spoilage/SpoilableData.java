package com.gregtechceu.gtceu.api.item.spoilage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;

@Accessors(fluent = true)
public class SpoilableData {

    // spotless:off
    public static final Codec<SpoilableData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("ticks").forGetter(SpoilableData::ticks)
    ).apply(instance, SpoilableData::new));
    // spotless:on

    @Getter
    public int ticks;

    public SpoilableData(int ticks) {

    }
}
