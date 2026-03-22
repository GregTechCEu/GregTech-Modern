package com.gregtechceu.gtceu.core.mixins.client;

import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractClientPlayer.class)
public interface AbstractClientPlayerAccessor {

    @Invoker("getPlayerInfo")
    PlayerInfo gtceu$getPlayerInfo();
}
