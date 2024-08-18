package com.gregtechceu.gtceu.data.material;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

import java.util.List;
import java.util.Optional;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote GTFoods
 */
public class GTFoods {

    public final static FoodProperties CHOCOLATE = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1), 0.1F)
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public final static FoodProperties DRINK = new FoodProperties.Builder()
            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 800, 1), 0.9F)
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public static final FoodProperties ANTIDOTE = new FoodProperties(0, 0, true, 0.2f, Optional.empty(), List.of());

    public static void init() {}
}
