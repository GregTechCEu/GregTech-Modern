package com.gregtechceu.gtceu.core.mixins;

import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "causeFallDamage",
                           at = @At(value = "INVOKE",
                                    target = "Lnet/minecraft/world/entity/LivingEntity;calculateFallDamage(FF)I"))
    private int gtceu$modifyPlayerFallDamage(int damage, @Local(argsOnly = true) DamageSource source) {
        LivingEntity self = (LivingEntity) (Object) this;

        // Check if the boots can absorb the fall damage
        ItemStack boots = self.getItemBySlot(EquipmentSlot.FEET);
        if (damage > 0 && boots.is(CustomTags.STEP_BOOTS) && boots.getItem() instanceof ArmorComponentItem armor) {
            damage -= armor.getArmorLogic().absorbDamage(self, boots, source, damage, EquipmentSlot.FEET,
                    level().isClientSide());
        }
        return damage;
    }
}
