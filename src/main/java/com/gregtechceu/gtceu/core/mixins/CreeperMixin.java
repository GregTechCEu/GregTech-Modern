package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.item.tool.behavior.LighterBehavior;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin {

    @Shadow
    public abstract void ignite();

    @Inject(method = "mobInteract", at = @At(value = "HEAD"))
    protected void gtceu$mobInteract(Player player, InteractionHand hand,
                                     CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof ComponentItem compItem) {
            for (var component : compItem.getComponents()) {
                if (component instanceof LighterBehavior lighter && lighter.consumeFuel(player, stack)) {
                    player.level().playSound(null, player.getOnPos(), SoundEvents.FLINTANDSTEEL_USE,
                            SoundSource.PLAYERS, 1.0F, GTValues.RNG.nextFloat() * 0.4F + 0.8F);
                    player.swing(hand);
                    ignite();
                    return;
                }
            }
        }
    }
}
