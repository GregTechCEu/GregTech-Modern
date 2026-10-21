package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.capability.GTCapabilityHelper;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.module.IJumpBoostItemModule;
import com.gregtechceu.gtceu.api.item.module.IModularItem;
import com.gregtechceu.gtceu.api.item.module.ModuleContext;
import com.gregtechceu.gtceu.utils.GTMath;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
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
                armorItem.damageItem(itemStack, (int) armorDamage, (LivingEntity) (Object) this, entity -> {});
            }
            ++i;
        }
    }

    @Unique
    private LivingEntity gtceu$self() {
        return (LivingEntity) (Object) this;
    }

    @ModifyReturnValue(method = "getJumpPower(F)F", at = @At("RETURN"))
    private float gtceu$adjustJumpHeight(float original) {
        float height = 0;
        for (ItemStack stack : this.getArmorSlots()) {
            IModularItem modularItem = GTCapabilityHelper.getModularItem(stack);
            if (modularItem == null) continue;
            for (ModuleContext module : modularItem.getAllModuleInstances()) {
                if (module.getModule().isEnabled(module) &&
                        module.getModule() instanceof IJumpBoostItemModule jumpBoostModule) {
                    height += jumpBoostModule.getJumpHeight(module);
                }
            }
        }
        return GTMath.addJumpHeight(original, gtceu$self().getGravity(), height);
    }
}
