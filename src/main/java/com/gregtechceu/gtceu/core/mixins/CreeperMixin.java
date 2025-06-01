package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.item.tool.behavior.LighterBehavior;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Creeper.class)
public abstract class CreeperMixin extends Monster implements PowerableMob {

    protected CreeperMixin(EntityType<? extends Monster> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract void ignite();

    // If it is a lighter and has fuel, ignite and then immediately exit. else if not enough fuel, cancel igniting.
    @Inject(method = "mobInteract",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z", ordinal = 0),
            cancellable = true)
    private void gtceu$lighterIgnite(Player player, InteractionHand hand,
                                     CallbackInfoReturnable<InteractionResult> cir,
                                     @Local ItemStack stack) {
        if (stack.getItem() instanceof ComponentItem compItem) {
            for (var component : compItem.getComponents()) {
                if (component instanceof LighterBehavior lighter) {
                    if (lighter.consumeFuel(player, stack)) {
                        if (!this.level().isClientSide) ignite();
                        cir.setReturnValue(InteractionResult.sidedSuccess(this.level().isClientSide));
                    } else {
                        cir.setReturnValue(InteractionResult.PASS);
                    }
                    return;
                }
            }
        }
    }
}
