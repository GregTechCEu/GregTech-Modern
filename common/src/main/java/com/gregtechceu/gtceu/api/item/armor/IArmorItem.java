package com.gregtechceu.gtceu.api.item.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IArmorItem {

    default int getArmorLayersAmount(ItemStack itemStack) {
        return 1;
    }

    default int getArmorLayerColor(ItemStack itemStack, int layerIndex) {
        return 0xFFFFFF;
    }

    void damageArmor(LivingEntity entity, ItemStack itemStack, DamageSource source, int damage, int slot);
}
