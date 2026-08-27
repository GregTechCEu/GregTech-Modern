package com.gregtechceu.gtceu.api.item.spoilage;

import com.gregtechceu.gtceu.utils.codec.GTCodecUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Range;

import java.util.List;

@Accessors(fluent = true)
public class ItemSpoilBehaviour {

    // spotless:off
    public static final Codec<ItemSpoilBehaviour> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GTCodecUtils.NON_NEGATIVE_LONG.fieldOf("ticks").forGetter(ItemSpoilBehaviour::ticks),
            Codec.list(SpoilAction.CODEC, 1, Integer.MAX_VALUE).fieldOf("spoil_actions").forGetter(ItemSpoilBehaviour::spoilActions)
    ).apply(instance, ItemSpoilBehaviour::new));
    // spotless:on

    @Getter
    @Range(from = 0, to = Long.MAX_VALUE)
    public long ticks;
    @Getter
    public List<SpoilAction> spoilActions;

    public ItemSpoilBehaviour(@Range(from = 0, to = Long.MAX_VALUE) long ticks, SpoilAction... spoilActions) {
        Validate.exclusiveBetween(0, Long.MAX_VALUE, ticks, "Spoilage ticks must be greater than 0.");
        this.ticks = ticks;
        this.spoilActions = List.of(spoilActions);
    }

    public ItemSpoilBehaviour(@Range(from = 0, to = Long.MAX_VALUE) long ticks, List<SpoilAction> spoilActions) {
        Validate.exclusiveBetween(0, Long.MAX_VALUE, ticks, "Spoilage ticks must be greater than 0.");
        this.ticks = ticks;
        this.spoilActions = spoilActions;
    }
}
