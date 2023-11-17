package com.gregtechceu.gtceu.test;

import com.gregtechceu.gtceu.test.api.machine.trait.RecipeLogicTest;

import java.util.function.Consumer;

public class GTGameTests {

    public static void registerTests(Consumer<Class<?>> consumer) {
        consumer.accept(RecipeLogicTest.class);
    }
}
