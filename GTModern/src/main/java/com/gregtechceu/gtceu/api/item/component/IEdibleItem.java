package com.gregtechceu.gtceu.api.item.component;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public interface IEdibleItem {

    FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity);

    boolean isEdible();

    default SoundEvent getEatingSound() {
        return SoundEvents.GENERIC_EAT;
    }

    default SoundEvent getDrinkingSound() {
        return SoundEvents.GENERIC_DRINK;
    }
}
