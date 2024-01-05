package com.gregtechceu.gtceu.fabric.core.mixins;

import com.gregtechceu.gtceu.api.item.fabric.IGTFabricItem;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow public abstract Item getItem();

    @Inject(method = "getMaxStackSize", at = @At("RETURN"), cancellable = true)
    private void gtceu$stackSensitiveMaxSize(CallbackInfoReturnable<Integer> cir) {
        if(this.getItem() instanceof IGTFabricItem gtItem) {
            cir.setReturnValue(gtItem.getMaxStackSize((ItemStack) (Object) this));
        }
    }

    @Inject(method = "getDamageValue", at = @At("RETURN"), cancellable = true)
    private void gtceu$stackSensitiveDamageValue(CallbackInfoReturnable<Integer> cir) {
        if(this.getItem() instanceof IGTFabricItem gtItem) {
            cir.setReturnValue(gtItem.getDamage((ItemStack) (Object) this));
        }
    }

    @Inject(method = "getMaxDamage", at = @At("RETURN"), cancellable = true)
    private void gtceu$stackSensitiveMaxDamage(CallbackInfoReturnable<Integer> cir) {
        if(this.getItem() instanceof IGTFabricItem gtItem) {
            cir.setReturnValue(gtItem.getMaxDamage((ItemStack) (Object) this));
        }
    }

    @Inject(method = "setDamageValue", at = @At("RETURN"), cancellable = true)
    private void gtceu$stackSensitiveSetDamage(int damage, CallbackInfo ci) {
        if(this.getItem() instanceof IGTFabricItem gtItem) {
            gtItem.setDamage((ItemStack) (Object) this, damage);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method = "isDamaged", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getDamageValue()I"))
    private int gtceu$stackSensitiveIsDamaged(int original) {
        if(this.getItem() instanceof IGTFabricItem gtItem) {
            return gtItem.isDamaged((ItemStack) (Object) this) ? 1 : 0;
        }
        return original;
    }
}
