package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.data.effect.GTMobEffects;

import net.minecraft.world.entity.player.Player;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.client.gui.Gui$HeartType")
public class GuiHeartTypeMixin {

    @ModifyExpressionValue(method = "forPlayer",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/player/Player;hasEffect(Lnet/minecraft/core/Holder;)Z",
                                    ordinal = 0))
    private static boolean gtceu$isPoisoned(boolean original, Player player) {
        return original || player.hasEffect(GTMobEffects.WEAK_POISON);
    }
}
