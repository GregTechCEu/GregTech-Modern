package com.gregtechceu.gtceu.api.item.tool.behavior;

import com.mojang.serialization.MapCodec;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

@AllArgsConstructor
public class ToolBehaviorType<T extends IToolBehavior<T>> {
    @Getter
    public final MapCodec<T> codec;
    @Getter
    public final StreamCodec<RegistryFriendlyByteBuf, T> streamCodec;
}
