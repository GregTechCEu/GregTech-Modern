package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.IComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.IEdibleItem;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
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

    @Shadow
    protected abstract SoundEvent getDrinkingSound(ItemStack stack);

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

    @WrapOperation(method = "eat(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/world/level/Level;playSound(Lnet/minecraft/world/entity/player/Player;DDDLnet/minecraft/sounds/SoundEvent;Lnet/minecraft/sounds/SoundSource;FF)V"))
    private void gtceu$adjustSound(Level instance, Player player, double x, double y, double z, SoundEvent sound,
                                   SoundSource category, float volume, float pitch, Operation<Void> original,
                                   Level level, ItemStack food) {
        if (food.getItem() instanceof IComponentItem componentItem) {
            for (var component : componentItem.getComponents()) {
                // If it's a drink, play drinking sound
                if (component instanceof IEdibleItem edible && edible.isDrink()) {
                    level.playSound(null, x, y, z, getDrinkingSound(food), category, volume, pitch);
                    return;
                }
            }
        }
        original.call(instance, player, x, y, z, sound, category, volume, pitch);
    }
}
