package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.module.AppliedItemModule;
import com.gregtechceu.gtceu.api.item.module.IJumpBoostItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Shadow
    public abstract Iterable<ItemStack> getArmorSlots();

    @Shadow
    public abstract void setItemSlot(EquipmentSlot slot, ItemStack stack);

    @Inject(method = "getDamageAfterArmorAbsorb",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/entity/LivingEntity;hurtArmor(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void gtceu$adjustArmorAbsorption(DamageSource damageSource, float damageAmount,
                                             CallbackInfoReturnable<Float> cir) {
        float armorDamage = Math.max(1.0F, damageAmount / 4.0F);
        int i = 0;
        for (ItemStack itemStack : this.getArmorSlots()) {
            if (itemStack.getItem() instanceof ArmorComponentItem armorItem) {
                EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i);
                armorItem.damageItem(itemStack, (int) armorDamage, (LivingEntity) (Object) this, entity -> {});
                if (itemStack.getCount() == 0) {
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            ++i;
        }
    }

    @Unique
    private LivingEntity gtceu$self() {
        return (LivingEntity) (Object) this;
    }

    @Inject(method = "getJumpBoostPower", at = @At("HEAD"), cancellable = true)
    private void gtceu$adjustJumpBoost(CallbackInfoReturnable<Float> cir) {
        float add = 0;
        for (ItemStack stack : this.getArmorSlots()) {
            IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
            if (modularItem == null) continue;
            for (AppliedItemModule module : modularItem.getAppliedModules()) {
                if (module.getModule() instanceof IJumpBoostItemModule jumpBoostModule) {
                    add += jumpBoostModule.getJumpBoost(module);
                }
            }
        }
        cir.setReturnValue(add + (gtceu$self().hasEffect(MobEffects.JUMP) ?
                0.1F * ((float) gtceu$self().getEffect(MobEffects.JUMP).getAmplifier() + 1.0F) : 0.0F));
        cir.cancel();
    }
}
