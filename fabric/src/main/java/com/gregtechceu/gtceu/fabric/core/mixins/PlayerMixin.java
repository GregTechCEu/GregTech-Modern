package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {

    @ModifyExpressionValue(method = "blockUsingShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;canDisableShield()Z"))
    private boolean gtceu$stackSensitiveBlockShield(boolean original, LivingEntity attacker) {
        if (attacker.getMainHandItem().getItem() instanceof IGTFabricItem gtItem) {
            return gtItem.canDisableShield(attacker.getMainHandItem(), ((LivingEntityAccessor)this).getUseItem(), (LivingEntity) (Object) this, attacker);
        }
        return original;
    }
}
