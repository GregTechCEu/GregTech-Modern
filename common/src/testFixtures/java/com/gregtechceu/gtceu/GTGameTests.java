package com.gregtechceu.gtceu;


import com.gregtechceu.gtceu.api.machine.trait.RecipeLogicTest;

import java.util.function.Consumer;

public class GTGameTests {

    public static void registerTests(Consumer<Class<?>> testRegisterConsumer) {
        testRegisterConsumer.accept(RecipeLogicTest.class);
    }
}
