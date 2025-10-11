package com.gregtechceu.gtceu.api.mui.base;

import net.minecraft.network.FriendlyByteBuf;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface ISyncedAction {

    @ApiStatus.OverrideOnly
    void invoke(@NotNull FriendlyByteBuf packet);
}
