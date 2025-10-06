package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerListener;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccessor {

    @Accessor
    ContainerListener getContainerListener();
}
