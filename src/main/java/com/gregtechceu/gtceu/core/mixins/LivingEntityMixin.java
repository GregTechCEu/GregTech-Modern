package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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
                     target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"))
    private void gtceu$adjustArmorAbsorption(DamageSource damageSource, float damageAmount,
                                             CallbackInfoReturnable<Float> cir) {
        float armorDamage = Math.max(1.0F, damageAmount / 4.0F);
        int i = 0;
        for (ItemStack itemStack : this.getArmorSlots()) {
            if (itemStack.getItem() instanceof ArmorComponentItem armorItem) {
                EquipmentSlot slot = EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i);
                armorItem.damageArmor((LivingEntity) (Object) this, itemStack, damageSource, (int) armorDamage, slot);
                if (itemStack.getCount() == 0) {
                    this.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            ++i;
        }
    }
}
