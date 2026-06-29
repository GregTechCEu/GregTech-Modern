package com.gregtechceu.gtceu.core.mixins.sable;

import com.gregtechceu.gtceu.api.mui.factory.CoverUIFactory;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.player.Player;

import brachy.modularui.factory.SidedPosGuiData;
import dev.ryanhcode.sable.Sable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Covers on a contraption block sit at the same far-off sub-level coordinates as the machine they ride
 * on, basically means that they are technically farther than 8 blocks away, so we need to make a check for it
 */
@Mixin(value = CoverUIFactory.class, remap = false)
public abstract class CoverUIFactorySubLevelRangeMixin {

    @Inject(
            method = "canInteractWith(Lnet/minecraft/world/entity/player/Player;Lbrachy/modularui/factory/SidedPosGuiData;)Z",
            at = @At("RETURN"),
            remap = false,
            cancellable = true)
    private void gtceu$allowSubLevelInteract(Player player, SidedPosGuiData guiData,
                                             CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) {
            return;
        }
        if (Sable.HELPER.getContaining(guiData.getLevel(), (Vec3i) guiData.getBlockPos()) != null) {
            cir.setReturnValue(true);
        }
    }
}
