package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.utils.JetpackHelper;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Abilities;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Shadow
    public ServerPlayer player;

    // Prevents player from getting kicked with "flying is not enabled on this server" when using jetpack
    @WrapOperation(method = "handleMovePlayer",
                   at = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/entity/player/Abilities;mayfly:Z",
                            opcode = Opcodes.GETFIELD))
    private boolean gtceu$shouldPreventClientFloatingCheck(Abilities instance, Operation<Boolean> original) {
        return original.call(instance) || JetpackHelper.isFlyingWithJetpack(this.player);
    }
}
