package com.gregtechceu.gtceu.core.mixins;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerGamePacketListenerImpl.class)
public interface ServerPlayNetworkHandlerAccessor {
    @Accessor("aboveGroundTickCount")
    void setFloatingTicks(int ticks);
}
