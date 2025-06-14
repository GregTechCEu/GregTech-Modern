package com.gregtechceu.gtceu.common.data.materials;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class GTFoods {

    public final static FoodProperties CHOCOLATE = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1), 0.1F)
            .alwaysEat().nutrition(4).saturationMod(0.3F).build();

    public final static FoodProperties DRINK = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 800, 1), 0.9F)
            .alwaysEat().nutrition(4).saturationMod(0.3F).build();

    public static final FoodProperties ANTIDOTE = new FoodProperties.Builder()
            .alwaysEat().fast().build();

    public static void init() {}
}
