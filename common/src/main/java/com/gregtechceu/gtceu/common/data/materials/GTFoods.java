package com.gregtechceu.gtceu.common.data.materials;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

/**
 * @author KilaBash
 * @date 2023/2/22
 * @implNote GTFoods
 */
public class GTFoods {
    public final static FoodProperties CHOCOLATE = new FoodProperties.Builder()
            .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 1), 0.1f)
            .alwaysEat().nutrition(4).saturationMod(0.3F).build();

    public final static FoodProperties DRINK = new FoodProperties.Builder()
            .effect(new MobEffectInstance(MobEffects.HEAL, 200, 1), 0.1f)
            .alwaysEat().nutrition(4).saturationMod(0.3F).build();

    public static void init() {

    }

}
