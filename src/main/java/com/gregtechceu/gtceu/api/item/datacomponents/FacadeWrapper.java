package com.gregtechceu.gtceu.api.item.datacomponents;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;

import com.mojang.serialization.Codec;

public record FacadeWrapper(BlockState state) {

    public static final Codec<FacadeWrapper> CODEC = BlockState.CODEC.xmap(FacadeWrapper::new, FacadeWrapper::state);
    public static final StreamCodec<RegistryFriendlyByteBuf, FacadeWrapper> STREAM_CODEC = ByteBufCodecs
            .fromCodecWithRegistries(CODEC);

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof FacadeWrapper that))
            return false;

        return state == that.state;
    }
}
