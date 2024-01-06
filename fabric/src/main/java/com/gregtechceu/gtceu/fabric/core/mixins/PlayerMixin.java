package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.gregtechceu.gtceu.common.item.tool.ToolEventHandlers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    @ModifyExpressionValue(method = "blockUsingShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canDisableShield()Z"))
    private boolean gtceu$stackSensitiveBlockShield(boolean original, LivingEntity attacker) {
        if (attacker.getMainHandItem().getItem() instanceof IGTFabricItem gtItem) {
            return gtItem.canDisableShield(attacker.getMainHandItem(), ((LivingEntityAccessor)this).getUseItem(), (LivingEntity) (Object) this, attacker);
        }
        return original;
    }

    @Inject(method = "interactOn", at = @At(value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
        ordinal = 0),
        cancellable = true)
    private void gtceu$toolEntityInteract(Entity entity, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult result = ToolEventHandlers.onPlayerEntityInteract((Player) (Object) this, interactionHand, entity);
        if (result != InteractionResult.PASS) {
            cir.setReturnValue(result);
        }
    }
}
