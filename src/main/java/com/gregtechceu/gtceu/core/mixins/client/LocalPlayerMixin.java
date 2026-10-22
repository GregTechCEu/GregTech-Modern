package com.gregtechceu.gtceu.core.mixins.client;

import com.gregtechceu.gtceu.api.item.armor.ArmorUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin {

    @Shadow
    public Input input;

    @Shadow
    @Final
    protected Minecraft minecraft;
    //Note: We strictly check for if the sprint key is down, double-tapping any strafe key would possibly start
    // Sprinting and would feel very awful for most players
    @ModifyReturnValue(method = "hasEnoughImpulseToStartSprinting", at = @At("RETURN"))
    private boolean gtceu$startOmnidirectionalSprint(boolean original) {
        return original || minecraft.options.keySprint.isDown() &&
                input.getMoveVector().length() >= 0.8F &&
                ArmorUtils.canSprintInAnyDirection((LocalPlayer) (Object) this);
    }

    @ModifyExpressionValue(method = "aiStep",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/client/player/Input;hasForwardImpulse()Z"))
    private boolean gtceu$keepOmnidirectionalSprint(boolean original) {
        return original || (input.forwardImpulse != 0 || input.leftImpulse != 0) &&
                ArmorUtils.canSprintInAnyDirection((LocalPlayer) (Object) this);
    }
}
