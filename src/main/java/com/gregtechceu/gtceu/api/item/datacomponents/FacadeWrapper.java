package com.gregtechceu.gtceu.api.item.datacomponents;

import com.gregtechceu.gtceu.utils.ItemStackHashStrategy;
import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record FacadeWrapper(ItemStack stack) {
    private static final ItemStackHashStrategy HASH_STRATEGY = ItemStackHashStrategy.comparingAll();
    public static final Codec<FacadeWrapper> CODEC = ItemStack.CODEC.xmap(FacadeWrapper::new, FacadeWrapper::stack);
    public static final StreamCodec<RegistryFriendlyByteBuf, FacadeWrapper> STREAM_CODEC = ItemStack.STREAM_CODEC.map(FacadeWrapper::new, FacadeWrapper::stack);

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FacadeWrapper that))
            return false;

        return ItemStack.matches(stack, that.stack);
    }

    @Override
    public int hashCode() {
        return HASH_STRATEGY.hashCode(stack);
    }
}
