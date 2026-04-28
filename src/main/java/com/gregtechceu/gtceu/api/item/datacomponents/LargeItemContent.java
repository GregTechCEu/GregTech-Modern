package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.utils.codec.CodecUtils;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.With;

public record LargeItemContent(@With ItemStack stored, @With long amount) {

    // spotless:off
    public static final Codec<ItemStack> OPTIONAL_SINGLE_ITEM_CODEC = ItemStack.OPTIONAL_CODEC;
    public static final Codec<LargeItemContent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            OPTIONAL_SINGLE_ITEM_CODEC.fieldOf("stored").forGetter(LargeItemContent::stored),
            CodecUtils.NON_NEGATIVE_LONG.fieldOf("amount").forGetter(LargeItemContent::amount)
    ).apply(instance, LargeItemContent::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, LargeItemContent> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_STREAM_CODEC, LargeItemContent::stored,
            ByteBufCodecs.VAR_LONG, LargeItemContent::amount,
            LargeItemContent::new);
    // spotless:on

    public static final LargeItemContent EMPTY = new LargeItemContent(ItemStack.EMPTY, 0);

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LargeItemContent(ItemStack otherStored, long otherAmount)))
            return false;

        return amount() == otherAmount &&
                ItemStack.isSameItemSameComponents(stored, otherStored);
    }

    @Override
    public int hashCode() {
        int result = ItemStack.hashItemAndComponents(stored());
        result = 31 * result + Long.hashCode(amount());
        return result;
    }
}
