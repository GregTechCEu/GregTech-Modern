package com.gregtechceu.gtceu.common.data.materials;

import net.minecraft.world.food.FoodProperties;

public class GTFoods {

    public final static FoodProperties CHOCOLATE = new FoodProperties.Builder()
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public final static FoodProperties DRINK = new FoodProperties.Builder()
            .alwaysEdible().nutrition(4).saturationModifier(0.3F).build();

    public static final FoodProperties ANTIDOTE = new FoodProperties.Builder()
            .alwaysEdible().nutrition(0).saturationModifier(0).build();

    public static void init() {}
}
