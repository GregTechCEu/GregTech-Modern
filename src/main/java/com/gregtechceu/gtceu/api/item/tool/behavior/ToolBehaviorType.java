package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.mojang.serialization.MapCodec;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ToolBehaviorType<T extends IToolBehavior<T>> {

    @Getter
    public final Codec<T> codec;
    @Getter
    public final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
}
